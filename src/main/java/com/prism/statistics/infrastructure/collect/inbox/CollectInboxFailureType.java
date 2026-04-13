package com.prism.statistics.infrastructure.collect.inbox;

public enum CollectInboxFailureType {

    RETRYABLE,
    PROCESSING_TIMEOUT,
    BUSINESS_INVARIANT,
    RETRY_EXHAUSTED;

    public boolean allowedInRetryPending() {
        return this == RETRYABLE || this == PROCESSING_TIMEOUT;
    }

    public boolean allowedInFailed() {
        return this == BUSINESS_INVARIANT || this == RETRY_EXHAUSTED;
    }
}
