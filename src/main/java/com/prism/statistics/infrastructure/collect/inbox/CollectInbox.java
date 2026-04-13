package com.prism.statistics.infrastructure.collect.inbox;

import com.prism.statistics.infrastructure.common.BoxEventTime;
import com.prism.statistics.infrastructure.common.BoxProcessingLease;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CollectInbox {

    private final Long id;
    private final CollectInboxType collectType;
    private final Long projectId;
    private final long runId;
    private final String payloadJson;

    private CollectInboxStatus status;
    private int processingAttempt;
    private BoxProcessingLease processingLease;
    private BoxEventTime processedTime;
    private BoxEventTime failedTime;
    private CollectInboxFailureSnapshot failure;

    public static CollectInbox pending(
            CollectInboxType collectType,
            Long projectId,
            long runId,
            String payloadJson
    ) {
        validateCollectType(collectType);
        validateRunId(runId);
        validatePayloadJson(payloadJson);

        return new CollectInbox(
                null,
                collectType,
                projectId,
                runId,
                payloadJson,
                CollectInboxStatus.PENDING,
                0,
                BoxProcessingLease.idle(),
                BoxEventTime.absent(),
                BoxEventTime.absent(),
                CollectInboxFailureSnapshot.absent()
        );
    }

    @Builder(builderMethodName = "rehydrateBuilder")
    private static CollectInbox rehydrate(
            Long id,
            CollectInboxType collectType,
            Long projectId,
            long runId,
            String payloadJson,
            CollectInboxStatus status,
            int processingAttempt,
            BoxProcessingLease processingLease,
            BoxEventTime processedTime,
            BoxEventTime failedTime,
            CollectInboxFailureSnapshot failure
    ) {
        validateCollectType(collectType);
        validateRunId(runId);
        validatePayloadJson(payloadJson);
        validateStatus(status);
        validateProcessingAttempt(processingAttempt);
        validateProcessingLease(processingLease);
        validateProcessedTime(processedTime);
        validateFailedTime(failedTime);
        validateFailure(failure);
        validateState(status, processingAttempt, processingLease, processedTime, failedTime, failure);

        return new CollectInbox(
                id,
                collectType,
                projectId,
                runId,
                payloadJson,
                status,
                processingAttempt,
                processingLease,
                processedTime,
                failedTime,
                failure
        );
    }

    private CollectInbox(
            Long id,
            CollectInboxType collectType,
            Long projectId,
            long runId,
            String payloadJson,
            CollectInboxStatus status,
            int processingAttempt,
            BoxProcessingLease processingLease,
            BoxEventTime processedTime,
            BoxEventTime failedTime,
            CollectInboxFailureSnapshot failure
    ) {
        this.id = id;
        this.collectType = collectType;
        this.projectId = projectId;
        this.runId = runId;
        this.payloadJson = payloadJson;
        this.status = status;
        this.processingAttempt = processingAttempt;
        this.processingLease = processingLease;
        this.processedTime = processedTime;
        this.failedTime = failedTime;
        this.failure = failure;
    }

    public void markProcessed(Instant processedAt) {
        validateProcessedAt(processedAt);
        validateTransition(CollectInboxStatus.PROCESSING, "PROCESSED");

        this.status = CollectInboxStatus.PROCESSED;
        this.processingLease = BoxProcessingLease.idle();
        this.processedTime = BoxEventTime.present(processedAt);
        this.failedTime = BoxEventTime.absent();
        this.failure = CollectInboxFailureSnapshot.absent();
    }

    public void markRetryPending(Instant failedAt, String failureReason) {
        markRetryPending(failedAt, failureReason, CollectInboxFailureType.RETRYABLE);
    }

    public void markRetryPending(
            Instant failedAt,
            String failureReason,
            CollectInboxFailureType failureType
    ) {
        validateFailedAt(failedAt);
        validateFailureReason(failureReason);
        validateRetryPendingFailureType(failureType);
        validateTransition(CollectInboxStatus.PROCESSING, "RETRY_PENDING");

        this.status = CollectInboxStatus.RETRY_PENDING;
        this.processingLease = BoxProcessingLease.idle();
        this.processedTime = BoxEventTime.absent();
        this.failedTime = BoxEventTime.present(failedAt);
        this.failure = CollectInboxFailureSnapshot.present(failureReason, failureType);
    }

    public void markFailed(
            Instant failedAt,
            String failureReason,
            CollectInboxFailureType failureType
    ) {
        validateFailedAt(failedAt);
        validateFailureReason(failureReason);
        validateFailedFailureType(failureType);
        validateTransition(CollectInboxStatus.PROCESSING, "FAILED");

        this.status = CollectInboxStatus.FAILED;
        this.processingLease = BoxProcessingLease.idle();
        this.processedTime = BoxEventTime.absent();
        this.failedTime = BoxEventTime.present(failedAt);
        this.failure = CollectInboxFailureSnapshot.present(failureReason, failureType);
    }

    private static void validateCollectType(CollectInboxType collectType) {
        if (collectType == null) {
            throw new IllegalArgumentException("collectType은 비어 있을 수 없습니다.");
        }
    }

    private static void validateRunId(long runId) {
        if (runId <= 0) {
            throw new IllegalArgumentException("runId는 0보다 커야 합니다.");
        }
    }

    private static void validatePayloadJson(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            throw new IllegalArgumentException("payloadJson은 비어 있을 수 없습니다.");
        }
    }

    private static void validateStatus(CollectInboxStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status는 비어 있을 수 없습니다.");
        }
    }

    private static void validateProcessingAttempt(int processingAttempt) {
        if (processingAttempt < 0) {
            throw new IllegalArgumentException("processingAttempt는 0 이상이어야 합니다.");
        }
    }

    private static void validateProcessingLease(BoxProcessingLease processingLease) {
        if (processingLease == null) {
            throw new IllegalArgumentException("processingLease는 비어 있을 수 없습니다.");
        }
    }

    private static void validateProcessedTime(BoxEventTime processedTime) {
        if (processedTime == null) {
            throw new IllegalArgumentException("processedTime은 비어 있을 수 없습니다.");
        }
    }

    private static void validateFailedTime(BoxEventTime failedTime) {
        if (failedTime == null) {
            throw new IllegalArgumentException("failedTime은 비어 있을 수 없습니다.");
        }
    }

    private static void validateFailure(CollectInboxFailureSnapshot failure) {
        if (failure == null) {
            throw new IllegalArgumentException("failure는 비어 있을 수 없습니다.");
        }
    }

    private static void validateState(
            CollectInboxStatus status,
            int processingAttempt,
            BoxProcessingLease processingLease,
            BoxEventTime processedTime,
            BoxEventTime failedTime,
            CollectInboxFailureSnapshot failure
    ) {
        if (status == CollectInboxStatus.PENDING) {
            validatePendingState(processingAttempt, processingLease, processedTime, failedTime, failure);
            return;
        }
        if (status == CollectInboxStatus.PROCESSING) {
            validateProcessingState(processingAttempt, processingLease, processedTime, failedTime, failure);
            return;
        }
        if (status == CollectInboxStatus.PROCESSED) {
            validateProcessedState(processingAttempt, processingLease, processedTime, failedTime, failure);
            return;
        }
        if (status == CollectInboxStatus.RETRY_PENDING) {
            validateRetryPendingState(processingAttempt, processingLease, processedTime, failedTime, failure);
            return;
        }

        validateFailedState(processingAttempt, processingLease, processedTime, failedTime, failure);
    }

    private static void validatePendingState(
            int processingAttempt,
            BoxProcessingLease processingLease,
            BoxEventTime processedTime,
            BoxEventTime failedTime,
            CollectInboxFailureSnapshot failure
    ) {
        if (processingAttempt != 0) {
            throw new IllegalArgumentException("PENDING 상태의 processingAttempt는 0이어야 합니다.");
        }
        validateIdleLease(processingLease, "PENDING");
        validateProcessedTimeAbsent(processedTime, "PENDING");
        validateFailedStateAbsent(failedTime, failure, "PENDING");
    }

    private static void validateProcessingState(
            int processingAttempt,
            BoxProcessingLease processingLease,
            BoxEventTime processedTime,
            BoxEventTime failedTime,
            CollectInboxFailureSnapshot failure
    ) {
        validateProcessedAttemptStarted(processingAttempt, "PROCESSING");
        if (!processingLease.isClaimed()) {
            throw new IllegalArgumentException("PROCESSING 상태는 processingLease를 보유해야 합니다.");
        }
        validateProcessedTimeAbsent(processedTime, "PROCESSING");
        validateFailedStateAbsent(failedTime, failure, "PROCESSING");
    }

    private static void validateProcessedState(
            int processingAttempt,
            BoxProcessingLease processingLease,
            BoxEventTime processedTime,
            BoxEventTime failedTime,
            CollectInboxFailureSnapshot failure
    ) {
        validateProcessedAttemptStarted(processingAttempt, "PROCESSED");
        validateIdleLease(processingLease, "PROCESSED");
        if (!processedTime.isPresent()) {
            throw new IllegalArgumentException("PROCESSED 상태는 processedTime이 있어야 합니다.");
        }
        validateFailedStateAbsent(failedTime, failure, "PROCESSED");
    }

    private static void validateRetryPendingState(
            int processingAttempt,
            BoxProcessingLease processingLease,
            BoxEventTime processedTime,
            BoxEventTime failedTime,
            CollectInboxFailureSnapshot failure
    ) {
        validateProcessedAttemptStarted(processingAttempt, "RETRY_PENDING");
        validateIdleLease(processingLease, "RETRY_PENDING");
        validateProcessedTimeAbsent(processedTime, "RETRY_PENDING");
        validateFailedStatePresent(failedTime, failure, "RETRY_PENDING");

        CollectInboxFailureType failureType = failure.type();
        if (failureType == CollectInboxFailureType.RETRYABLE
                || failureType == CollectInboxFailureType.PROCESSING_TIMEOUT) {
            return;
        }

        throw new IllegalArgumentException("RETRY_PENDING 상태의 failureType이 올바르지 않습니다.");
    }

    private static void validateFailedState(
            int processingAttempt,
            BoxProcessingLease processingLease,
            BoxEventTime processedTime,
            BoxEventTime failedTime,
            CollectInboxFailureSnapshot failure
    ) {
        validateProcessedAttemptStarted(processingAttempt, "FAILED");
        validateIdleLease(processingLease, "FAILED");
        validateProcessedTimeAbsent(processedTime, "FAILED");
        validateFailedStatePresent(failedTime, failure, "FAILED");

        CollectInboxFailureType failureType = failure.type();
        if (failureType == CollectInboxFailureType.BUSINESS_INVARIANT
                || failureType == CollectInboxFailureType.RETRY_EXHAUSTED) {
            return;
        }

        throw new IllegalArgumentException("FAILED 상태의 failureType이 올바르지 않습니다.");
    }

    private static void validateProcessedAttemptStarted(int processingAttempt, String statusName) {
        if (processingAttempt <= 0) {
            throw new IllegalArgumentException(statusName + " 상태의 processingAttempt는 1 이상이어야 합니다.");
        }
    }

    private static void validateIdleLease(BoxProcessingLease processingLease, String statusName) {
        if (processingLease.isClaimed()) {
            throw new IllegalArgumentException(statusName + " 상태는 processingLease가 비어 있어야 합니다.");
        }
    }

    private static void validateProcessedTimeAbsent(BoxEventTime processedTime, String statusName) {
        if (processedTime.isPresent()) {
            throw new IllegalArgumentException(statusName + " 상태는 processedTime이 비어 있어야 합니다.");
        }
    }

    private static void validateFailedStateAbsent(
            BoxEventTime failedTime,
            CollectInboxFailureSnapshot failure,
            String statusName
    ) {
        if (failedTime.isPresent()) {
            throw new IllegalArgumentException(statusName + " 상태는 failedTime이 비어 있어야 합니다.");
        }
        if (failure.isPresent()) {
            throw new IllegalArgumentException(statusName + " 상태는 failure가 비어 있어야 합니다.");
        }
    }

    private static void validateFailedStatePresent(
            BoxEventTime failedTime,
            CollectInboxFailureSnapshot failure,
            String statusName
    ) {
        if (!failedTime.isPresent()) {
            throw new IllegalArgumentException(statusName + " 상태는 failedTime이 있어야 합니다.");
        }
        if (!failure.isPresent()) {
            throw new IllegalArgumentException(statusName + " 상태는 failure가 있어야 합니다.");
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

    private void validateRetryPendingFailureType(CollectInboxFailureType failureType) {
        if (failureType != null && failureType.allowedInRetryPending()) {
            return;
        }

        throw new IllegalArgumentException("RETRY_PENDING failureType이 올바르지 않습니다.");
    }

    private void validateFailedFailureType(CollectInboxFailureType failureType) {
        if (failureType != null && failureType.allowedInFailed()) {
            return;
        }

        throw new IllegalArgumentException("FAILED failureType이 올바르지 않습니다.");
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
