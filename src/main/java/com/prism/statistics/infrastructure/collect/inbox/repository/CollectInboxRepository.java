package com.prism.statistics.infrastructure.collect.inbox.repository;

import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CollectInboxRepository {

    boolean enqueue(CollectInboxType collectType, Long projectId, Long runId, String payloadJson);

    List<CollectInbox> findClaimable(int limit);

    Optional<CollectInbox> findById(Long inboxId);

    boolean markProcessingIfClaimable(Long inboxId, Instant processingStartedAt);

    int recoverTimeoutProcessing(Instant processingStartedBefore, Instant failedAt, String failureReason, int maxAttempts);

    CollectInbox save(CollectInbox inbox);
}
