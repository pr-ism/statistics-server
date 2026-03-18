package com.prism.statistics.application.collect.inbox;

import com.prism.statistics.global.config.properties.CollectRetryProperties;
import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxFailureType;
import com.prism.statistics.infrastructure.collect.inbox.repository.CollectInboxRepository;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectInboxEntryProcessor {

    private static final String UNKNOWN_FAILURE_REASON = "unknown failure";

    private final Clock clock;
    private final CollectRetryProperties collectRetryProperties;
    private final CollectInboxRepository collectInboxRepository;
    private final CollectInboxClaimedExecutor collectInboxClaimedExecutor;
    private final CollectInboxFailureReasonTruncator failureReasonTruncator;
    private final CollectRetryExceptionClassifier retryExceptionClassifier;

    public void process(CollectInbox inbox) {
        Long inboxId = inbox.getId();
        if (inboxId == null) {
            return;
        }

        Instant processingStartedAt = clock.instant();
        if (!collectInboxRepository.markProcessingIfClaimable(inboxId, processingStartedAt)) {
            return;
        }

        collectInboxRepository.findById(inboxId)
                .ifPresentOrElse(
                        claimedInbox -> processClaimedInbox(claimedInbox),
                        () -> log.warn(
                                "PROCESSING으로 전이된 inbox를 조회하지 못했습니다. inboxId={}",
                                inboxId
                        )
                );
    }

    private void processClaimedInbox(CollectInbox claimedInbox) {
        try {
            collectInboxClaimedExecutor.execute(claimedInbox);
        } catch (Exception e) {
            log.error(
                    "{} inbox 처리에 실패했습니다. inboxId={}",
                    claimedInbox.getCollectType(),
                    claimedInbox.getId(),
                    e
            );

            markFailureStatus(claimedInbox, e);
            collectInboxRepository.save(claimedInbox);
        }
    }

    private void markFailureStatus(CollectInbox inbox, Exception exception) {
        String reason = resolveFailureReason(exception);

        if (!retryExceptionClassifier.isRetryable(exception)) {
            inbox.markFailed(clock.instant(), reason, CollectInboxFailureType.BUSINESS_INVARIANT);
            return;
        }

        if (inbox.getProcessingAttempt() < collectRetryProperties.maxAttempts()) {
            inbox.markRetryPending(clock.instant(), reason);
            return;
        }

        inbox.markFailed(clock.instant(), reason, CollectInboxFailureType.RETRY_EXHAUSTED);
    }

    private String resolveFailureReason(Exception exception) {
        String reason = failureReasonTruncator.truncate(exception.getMessage());

        if (reason == null || reason.isBlank()) {
            return UNKNOWN_FAILURE_REASON;
        }

        return reason;
    }
}
