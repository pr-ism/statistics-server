package com.prism.statistics.infrastructure.collect.inbox.persistence;

import static com.prism.statistics.infrastructure.collect.inbox.QCollectInbox.collectInbox;

import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxFailureType;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxStatus;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import com.prism.statistics.infrastructure.collect.inbox.repository.CollectInboxRepository;
import com.prism.statistics.infrastructure.common.MysqlDuplicateKeyDetector;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CollectInboxRepositoryAdapter implements CollectInboxRepository {

    private static final List<CollectInboxStatus> CLAIMABLE_STATUSES = List.of(
            CollectInboxStatus.PENDING,
            CollectInboxStatus.RETRY_PENDING
    );

    private final JPAQueryFactory queryFactory;
    private final JpaCollectInboxRepository repository;
    private final CollectInboxCreator inboxCreator;
    private final MysqlDuplicateKeyDetector mysqlDuplicateKeyDetector;

    @Override
    public boolean enqueue(CollectInboxType collectType, Long projectId, String idempotencyKey, String payloadJson) {
        CollectInbox inbox = CollectInbox.pending(collectType, projectId, idempotencyKey, payloadJson);

        try {
            inboxCreator.saveNew(inbox);
            return true;
        } catch (DataIntegrityViolationException exception) {
            if (!mysqlDuplicateKeyDetector.isDuplicateKey(exception)) {
                throw exception;
            }
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectInbox> findClaimable(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        return queryFactory
                .selectFrom(collectInbox)
                .where(collectInbox.status.in(CLAIMABLE_STATUSES))
                .orderBy(collectInbox.id.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CollectInbox> findById(Long inboxId) {
        return repository.findById(inboxId);
    }

    @Override
    @Transactional
    public boolean markProcessingIfClaimable(Long inboxId, Instant processingStartedAt) {
        long updatedCount = queryFactory
                .update(collectInbox)
                .set(collectInbox.status, CollectInboxStatus.PROCESSING)
                .set(collectInbox.processingAttempt, collectInbox.processingAttempt.add(1))
                .set(collectInbox.processingStartedAt, processingStartedAt)
                .set(collectInbox.failedAt, Expressions.nullExpression(Instant.class))
                .set(collectInbox.failureReason, Expressions.nullExpression(String.class))
                .set(collectInbox.failureType, Expressions.nullExpression(CollectInboxFailureType.class))
                .where(
                        collectInbox.id.eq(inboxId),
                        collectInbox.status.in(CLAIMABLE_STATUSES)
                )
                .execute();

        return updatedCount > 0;
    }

    @Override
    @Transactional
    public int recoverTimeoutProcessing(
            Instant processingStartedBefore,
            Instant failedAt,
            String failureReason,
            int maxAttempts
    ) {
        BooleanExpression timeoutCondition = collectInbox.processingStartedAt.isNull()
                .or(collectInbox.processingStartedAt.lt(processingStartedBefore));

        long exhaustedCount = queryFactory
                .update(collectInbox)
                .set(collectInbox.status, CollectInboxStatus.FAILED)
                .set(collectInbox.processingStartedAt, Expressions.nullExpression(Instant.class))
                .set(collectInbox.failedAt, failedAt)
                .set(collectInbox.failureReason, failureReason)
                .set(collectInbox.failureType, CollectInboxFailureType.RETRY_EXHAUSTED)
                .where(
                        collectInbox.status.eq(CollectInboxStatus.PROCESSING),
                        timeoutCondition,
                        collectInbox.processingAttempt.goe(maxAttempts)
                )
                .execute();

        long recoveredCount = queryFactory
                .update(collectInbox)
                .set(collectInbox.status, CollectInboxStatus.RETRY_PENDING)
                .set(collectInbox.processingStartedAt, Expressions.nullExpression(Instant.class))
                .set(collectInbox.failedAt, failedAt)
                .set(collectInbox.failureReason, failureReason)
                .set(collectInbox.failureType, Expressions.nullExpression(CollectInboxFailureType.class))
                .where(
                        collectInbox.status.eq(CollectInboxStatus.PROCESSING),
                        timeoutCondition,
                        collectInbox.processingAttempt.lt(maxAttempts)
                )
                .execute();

        return Math.toIntExact(exhaustedCount + recoveredCount);
    }

    @Override
    @Transactional
    public CollectInbox save(CollectInbox inbox) {
        return repository.save(inbox);
    }
}
