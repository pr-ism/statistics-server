package com.prism.statistics.infrastructure.collect.inbox.persistence;

import static com.prism.statistics.infrastructure.collect.inbox.persistence.QCollectInboxJpaEntity.collectInboxJpaEntity;

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
    public boolean enqueue(CollectInboxType collectType, Long projectId, long runId, String payloadJson) {
        CollectInbox inbox = CollectInbox.pending(collectType, projectId, runId, payloadJson);

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
                .selectFrom(collectInboxJpaEntity)
                .where(collectInboxJpaEntity.status.in(CLAIMABLE_STATUSES))
                .orderBy(collectInboxJpaEntity.id.asc())
                .limit(limit)
                .fetch()
                .stream()
                .map(inboxJpaEntity -> inboxJpaEntity.toDomain())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CollectInbox> findById(Long inboxId) {
        return repository.findDomainById(inboxId);
    }

    @Override
    @Transactional
    public boolean markProcessingIfClaimable(Long inboxId, Instant processingStartedAt) {
        long updatedCount = queryFactory
                .update(collectInboxJpaEntity)
                .set(collectInboxJpaEntity.status, CollectInboxStatus.PROCESSING)
                .set(
                        collectInboxJpaEntity.processingAttempt,
                        collectInboxJpaEntity.processingAttempt.add(1)
                )
                .set(collectInboxJpaEntity.processingStartedAt, processingStartedAt)
                .set(collectInboxJpaEntity.processedAt, Expressions.nullExpression(Instant.class))
                .set(collectInboxJpaEntity.failedAt, Expressions.nullExpression(Instant.class))
                .set(collectInboxJpaEntity.failureReason, Expressions.nullExpression(String.class))
                .set(
                        collectInboxJpaEntity.failureType,
                        Expressions.nullExpression(CollectInboxFailureType.class)
                )
                .where(
                        collectInboxJpaEntity.id.eq(inboxId),
                        collectInboxJpaEntity.status.in(CLAIMABLE_STATUSES)
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
        BooleanExpression timeoutCondition = collectInboxJpaEntity.processingStartedAt.isNull()
                .or(collectInboxJpaEntity.processingStartedAt.lt(processingStartedBefore));

        long exhaustedCount = queryFactory
                .update(collectInboxJpaEntity)
                .set(collectInboxJpaEntity.status, CollectInboxStatus.FAILED)
                .set(collectInboxJpaEntity.processingStartedAt, Expressions.nullExpression(Instant.class))
                .set(collectInboxJpaEntity.processedAt, Expressions.nullExpression(Instant.class))
                .set(collectInboxJpaEntity.failedAt, failedAt)
                .set(collectInboxJpaEntity.failureReason, failureReason)
                .set(collectInboxJpaEntity.failureType, CollectInboxFailureType.RETRY_EXHAUSTED)
                .where(
                        collectInboxJpaEntity.status.eq(CollectInboxStatus.PROCESSING),
                        timeoutCondition,
                        collectInboxJpaEntity.processingAttempt.goe(maxAttempts)
                )
                .execute();

        long recoveredCount = queryFactory
                .update(collectInboxJpaEntity)
                .set(collectInboxJpaEntity.status, CollectInboxStatus.RETRY_PENDING)
                .set(collectInboxJpaEntity.processingStartedAt, Expressions.nullExpression(Instant.class))
                .set(collectInboxJpaEntity.processedAt, Expressions.nullExpression(Instant.class))
                .set(collectInboxJpaEntity.failedAt, failedAt)
                .set(collectInboxJpaEntity.failureReason, failureReason)
                .set(collectInboxJpaEntity.failureType, CollectInboxFailureType.PROCESSING_TIMEOUT)
                .where(
                        collectInboxJpaEntity.status.eq(CollectInboxStatus.PROCESSING),
                        timeoutCondition,
                        collectInboxJpaEntity.processingAttempt.lt(maxAttempts)
                )
                .execute();

        return Math.toIntExact(exhaustedCount + recoveredCount);
    }

    @Override
    @Transactional
    public CollectInbox save(CollectInbox inbox) {
        Long inboxId = inbox.getId();
        if (inboxId == null) {
            CollectInboxJpaEntity newEntity = new CollectInboxJpaEntity();
            newEntity.apply(inbox);
            return repository.save(newEntity).toDomain();
        }

        CollectInboxJpaEntity persistedEntity = repository.findById(inboxId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 collectInbox입니다. id=" + inboxId));
        persistedEntity.apply(inbox);
        return repository.save(persistedEntity).toDomain();
    }
}
