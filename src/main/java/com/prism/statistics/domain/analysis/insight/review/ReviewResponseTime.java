package com.prism.statistics.domain.analysis.insight.review;

import com.prism.statistics.domain.analysis.insight.vo.DurationMinutes;
import com.prism.statistics.domain.common.BaseTimeEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "review_response_times")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewResponseTime extends BaseTimeEntity {

    private Long pullRequestId;

    @Embedded
    @AttributeOverride(name = "minutes", column = @Column(name = "response_after_review_minutes"))
    private DurationMinutes responseAfterReview;

    @Embedded
    @AttributeOverride(name = "minutes", column = @Column(name = "changes_resolution_minutes"))
    private DurationMinutes changesResolution;

    private LocalDateTime lastChangesRequestedAt;

    private LocalDateTime firstCommitAfterChangesAt;

    private LocalDateTime firstApproveAfterChangesAt;

    private int changesRequestedCount;

    public static ReviewResponseTime createWithoutChangesRequested(Long pullRequestId) {
        return ReviewResponseTime.builder()
                .pullRequestId(pullRequestId)
                .build();
    }

    public static ReviewResponseTime createOnChangesRequested(
            Long pullRequestId,
            LocalDateTime changesRequestedAt
    ) {
        return ReviewResponseTime.builder()
                .pullRequestId(pullRequestId)
                .lastChangesRequestedAt(changesRequestedAt)
                .changesRequestedCount(1)
                .build();
    }

    private void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("Pull Request ID는 필수입니다.");
        }
    }

    private void validateChangesRequestedCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Request Changes 횟수는 0보다 작을 수 없습니다.");
        }
    }

    @Builder
    private ReviewResponseTime(
            Long pullRequestId,
            DurationMinutes responseAfterReview,
            DurationMinutes changesResolution,
            LocalDateTime lastChangesRequestedAt,
            LocalDateTime firstCommitAfterChangesAt,
            LocalDateTime firstApproveAfterChangesAt,
            int changesRequestedCount
    ) {
        validatePullRequestId(pullRequestId);
        validateChangesRequestedCount(changesRequestedCount);
        this.pullRequestId = pullRequestId;
        this.responseAfterReview = responseAfterReview;
        this.changesResolution = changesResolution;
        this.lastChangesRequestedAt = lastChangesRequestedAt;
        this.firstCommitAfterChangesAt = firstCommitAfterChangesAt;
        this.firstApproveAfterChangesAt = firstApproveAfterChangesAt;
        this.changesRequestedCount = changesRequestedCount;
    }

    private static void validateDateTime(LocalDateTime dateTime, String fieldName) {
        if (dateTime == null) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
    }

    public void updateOnChangesRequested(LocalDateTime changesRequestedAt) {
        validateDateTime(changesRequestedAt, "변경 요청 시각");

        this.lastChangesRequestedAt = changesRequestedAt;
        this.changesRequestedCount++;
        this.firstCommitAfterChangesAt = null;
        this.firstApproveAfterChangesAt = null;
        this.responseAfterReview = null;
        this.changesResolution = null;
    }

    public void updateOnCommitAfterChanges(LocalDateTime committedAt) {
        validateDateTime(committedAt, "커밋 시각");

        if (this.lastChangesRequestedAt == null) {
            return;
        }

        if (this.firstCommitAfterChangesAt == null && committedAt.isAfter(this.lastChangesRequestedAt)) {
            this.firstCommitAfterChangesAt = committedAt;
            this.responseAfterReview = DurationMinutes.between(this.lastChangesRequestedAt, committedAt);
        }
    }

    public void updateOnApproveAfterChanges(LocalDateTime approvedAt) {
        validateDateTime(approvedAt, "승인 시각");
        
        if (this.lastChangesRequestedAt == null) {
            return;
        }

        if (this.firstApproveAfterChangesAt == null && approvedAt.isAfter(this.lastChangesRequestedAt)) {
            this.firstApproveAfterChangesAt = approvedAt;
            this.changesResolution = DurationMinutes.between(this.lastChangesRequestedAt, approvedAt);
        }
    }

    public boolean hasChangesRequested() {
        return changesRequestedCount > 0;
    }

    public boolean hasResponded() {
        return responseAfterReview != null;
    }

    public boolean isResolved() {
        return changesResolution != null;
    }
}
