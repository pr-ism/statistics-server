package com.prism.statistics.application.collect.inbox;

import com.prism.statistics.global.config.properties.CollectInboxProperties;
import com.prism.statistics.global.config.properties.CollectRetryProperties;
import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import com.prism.statistics.infrastructure.collect.inbox.repository.CollectInboxRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectInboxProcessor {

    private static final String PROCESSING_TIMEOUT_FAILURE_REASON =
            "PROCESSING 타임아웃으로 복구 처리되었습니다.";

    private final Clock clock;
    private final CollectInboxProperties collectInboxProperties;
    private final CollectRetryProperties collectRetryProperties;
    private final CollectInboxRepository collectInboxRepository;
    private final CollectInboxEntryProcessor collectInboxEntryProcessor;

    public boolean enqueue(
            CollectInboxType collectType,
            Long projectId,
            Long runId,
            String payloadJson
    ) {
        return collectInboxRepository.enqueue(collectType, projectId, runId, payloadJson);
    }

    public void processPending(int limit) {
        recoverTimeoutProcessing();

        List<CollectInbox> pendings = collectInboxRepository.findClaimable(limit);

        for (CollectInbox pending : pendings) {
            processSafely(pending);
        }
    }

    private void recoverTimeoutProcessing() {
        Instant now = clock.instant();
        int recoveredCount = collectInboxRepository.recoverTimeoutProcessing(
                now.minusMillis(collectInboxProperties.processingTimeoutMs()),
                now,
                PROCESSING_TIMEOUT_FAILURE_REASON,
                collectRetryProperties.maxAttempts()
        );

        if (recoveredCount > 0) {
            log.warn("collect inbox PROCESSING 고착 건을 복구했습니다. count={}", recoveredCount);
        }
    }

    private void processSafely(CollectInbox pending) {
        try {
            collectInboxEntryProcessor.process(pending);
        } catch (Exception e) {
            log.error(
                    "collect inbox 엔트리 처리 중 예상치 못한 오류가 발생했습니다. inboxId={}",
                    pending.getId(),
                    e
            );
        }
    }
}
