package com.prism.statistics.infrastructure.collect.inbox;

import com.prism.statistics.domain.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "collect_inbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectInbox extends BaseTimeEntity {

    @Enumerated(EnumType.STRING)
    private CollectInboxType collectType;

    private Long projectId;

    private Long runId;

    @Lob
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    private CollectInboxStatus status;

    private int processingAttempt;

    private Instant processingStartedAt;

    private Instant processedAt;

    private Instant failedAt;

    private String failureReason;

    @Enumerated(EnumType.STRING)
    private CollectInboxFailureType failureType;

    public static CollectInbox pending(
            CollectInboxType collectType,
            Long projectId,
            Long runId,
            String payloadJson
    ) {
        validateCollectType(collectType);
        validateRunId(runId);
        validatePayloadJson(payloadJson);

        return new CollectInbox(
                collectType,
                projectId,
                runId,
                payloadJson,
                CollectInboxStatus.PENDING,
                0
        );
    }

    private CollectInbox(
            CollectInboxType collectType,
            Long projectId,
            Long runId,
            String payloadJson,
            CollectInboxStatus status,
            int processingAttempt
    ) {
        this.collectType = collectType;
        this.projectId = projectId;
        this.runId = runId;
        this.payloadJson = payloadJson;
        this.status = status;
        this.processingAttempt = processingAttempt;
    }

    public void markProcessing(Instant processingStartedAt) {
        validateProcessingStartedAt(processingStartedAt);
        validateProcessingTransition();

        this.status = CollectInboxStatus.PROCESSING;
        this.processingAttempt += 1;
        this.processingStartedAt = processingStartedAt;
        this.failureReason = null;
        this.failureType = null;
        this.failedAt = null;
    }

    public void markProcessed(Instant processedAt) {
        validateProcessedAt(processedAt);
        validateTransition(CollectInboxStatus.PROCESSING, "PROCESSED");

        this.status = CollectInboxStatus.PROCESSED;
        this.processingStartedAt = null;
        this.processedAt = processedAt;
        this.failureReason = null;
        this.failureType = null;
        this.failedAt = null;
    }

    public void markRetryPending(Instant failedAt, String failureReason) {
        validateFailedAt(failedAt);
        validateFailureReason(failureReason);
        validateTransition(CollectInboxStatus.PROCESSING, "RETRY_PENDING");

        this.status = CollectInboxStatus.RETRY_PENDING;
        this.processingStartedAt = null;
        this.failedAt = failedAt;
        this.failureReason = failureReason;
        this.failureType = null;
    }

    public void markFailed(
            Instant failedAt,
            String failureReason,
            CollectInboxFailureType failureType
    ) {
        validateFailedAt(failedAt);
        validateFailureReason(failureReason);
        validateFailureType(failureType);
        validateTransition(CollectInboxStatus.PROCESSING, "FAILED");

        this.status = CollectInboxStatus.FAILED;
        this.processingStartedAt = null;
        this.failedAt = failedAt;
        this.failureReason = failureReason;
        this.failureType = failureType;
    }

    private static void validateCollectType(CollectInboxType collectType) {
        if (collectType == null) {
            throw new IllegalArgumentException("collectType은 비어 있을 수 없습니다.");
        }
    }

    private static void validateRunId(Long runId) {
        if (runId == null) {
            throw new IllegalArgumentException("runId는 비어 있을 수 없습니다.");
        }
    }

    private static void validatePayloadJson(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            throw new IllegalArgumentException("payloadJson은 비어 있을 수 없습니다.");
        }
    }

    private void validateProcessingStartedAt(Instant processingStartedAt) {
        if (processingStartedAt == null) {
            throw new IllegalArgumentException("processingStartedAt은 비어 있을 수 없습니다.");
        }
    }

    private void validateProcessedAt(Instant processedAt) {
        if (processedAt == null) {
            throw new IllegalArgumentException("processedAt은 비어 있을 수 없습니다.");
        }
    }

    private void validateFailedAt(Instant failedAt) {
        if (failedAt == null) {
            throw new IllegalArgumentException("failedAt은 비어 있을 수 없습니다.");
        }
    }

    private void validateFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            throw new IllegalArgumentException("failureReason은 비어 있을 수 없습니다.");
        }
    }

    private void validateFailureType(CollectInboxFailureType failureType) {
        if (failureType == null) {
            throw new IllegalArgumentException("failureType은 비어 있을 수 없습니다.");
        }
    }

    private void validateProcessingTransition() {
        if (this.status == CollectInboxStatus.PENDING
                || this.status == CollectInboxStatus.RETRY_PENDING) {
            return;
        }

        throw new IllegalStateException(
                "PROCESSING 전이는 PENDING 또는 RETRY_PENDING 상태에서만 가능합니다. 현재: " + this.status
        );
    }

    private void validateTransition(CollectInboxStatus expectedStatus, String targetStatus) {
        if (this.status == expectedStatus) {
            return;
        }

        throw new IllegalStateException(
                targetStatus + " 전이는 " + expectedStatus + " 상태에서만 가능합니다. 현재: " + this.status
        );
    }
}
