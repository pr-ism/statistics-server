package com.prism.statistics.infrastructure.collect.inbox.persistence;

import com.prism.statistics.domain.common.BaseTimeEntity;
import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxFailureType;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxStatus;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import com.prism.statistics.infrastructure.common.BoxEventTime;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxFailureSnapshot;
import com.prism.statistics.infrastructure.common.BoxProcessingLease;
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
public class CollectInboxJpaEntity extends BaseTimeEntity {

    @Enumerated(EnumType.STRING)
    private CollectInboxType collectType;

    private Long projectId;

    private long runId;

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

    public CollectInbox toDomain() {
        return CollectInbox.rehydrateBuilder()
                .id(getId())
                .collectType(collectType)
                .projectId(projectId)
                .runId(runId)
                .payloadJson(payloadJson)
                .status(status)
                .processingAttempt(processingAttempt)
                .processingLease(toProcessingLease())
                .processedTime(toProcessedTime())
                .failedTime(toFailedTime())
                .failure(toFailure())
                .build();
    }

    public void apply(CollectInbox inbox) {
        this.collectType = inbox.getCollectType();
        this.projectId = inbox.getProjectId();
        this.runId = inbox.getRunId();
        this.payloadJson = inbox.getPayloadJson();
        this.status = inbox.getStatus();
        this.processingAttempt = inbox.getProcessingAttempt();
        applyProcessingLease(inbox);
        applyProcessedTime(inbox);
        applyFailedTime(inbox);
        applyFailure(inbox);
    }

    private void applyProcessingLease(CollectInbox inbox) {
        this.processingStartedAt = null;
        if (inbox.getProcessingLease().isClaimed()) {
            this.processingStartedAt = inbox.getProcessingLease().startedAt();
        }
    }

    private void applyProcessedTime(CollectInbox inbox) {
        this.processedAt = null;
        if (inbox.getProcessedTime().isPresent()) {
            this.processedAt = inbox.getProcessedTime().occurredAt();
        }
    }

    private void applyFailedTime(CollectInbox inbox) {
        this.failedAt = null;
        if (inbox.getFailedTime().isPresent()) {
            this.failedAt = inbox.getFailedTime().occurredAt();
        }
    }

    private void applyFailure(CollectInbox inbox) {
        this.failureReason = null;
        this.failureType = null;

        CollectInboxFailureSnapshot failure = inbox.getFailure();
        if (!failure.isPresent()) {
            return;
        }

        this.failureReason = failure.reason();
        this.failureType = failure.type();
    }

    private BoxProcessingLease toProcessingLease() {
        if (processingStartedAt == null) {
            return BoxProcessingLease.idle();
        }

        return BoxProcessingLease.claimed(processingStartedAt);
    }

    private BoxEventTime toProcessedTime() {
        if (processedAt == null) {
            return BoxEventTime.absent();
        }

        return BoxEventTime.present(processedAt);
    }

    private BoxEventTime toFailedTime() {
        if (failedAt == null) {
            return BoxEventTime.absent();
        }

        return BoxEventTime.present(failedAt);
    }

    private CollectInboxFailureSnapshot toFailure() {
        if (failureReason == null && failureType == null) {
            return CollectInboxFailureSnapshot.absent();
        }
        if (failureReason == null || failureType == null) {
            throw new IllegalStateException("failure 상태가 올바르지 않습니다.");
        }

        return CollectInboxFailureSnapshot.present(failureReason, failureType);
    }
}
