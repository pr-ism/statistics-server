package com.prism.statistics.domain.analysis.insight.bottleneck;

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
@Table(name = "pull_request_bottlenecks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestBottleneck extends BaseTimeEntity {

    private Long pullRequestId;

    @Embedded
    @AttributeOverride(name = "minutes", column = @Column(name = "review_wait_minutes"))
    private DurationMinutes reviewWait;

    @Embedded
    @AttributeOverride(name = "minutes", column = @Column(name = "review_progress_minutes"))
    private DurationMinutes reviewProgress;

    @Embedded
    @AttributeOverride(name = "minutes", column = @Column(name = "merge_wait_minutes"))
    private DurationMinutes mergeWait;

    private LocalDateTime firstReviewAt;

    private LocalDateTime lastReviewAt;

    private LocalDateTime lastApproveAt;

    public static PullRequestBottleneck createWithoutReview(Long pullRequestId) {
        return PullRequestBottleneck.builder()
                .pullRequestId(pullRequestId)
                .reviewProgress(DurationMinutes.zero())
                .build();
    }

    public static PullRequestBottleneck createOnFirstReview(
            Long pullRequestId,
            LocalDateTime reviewReadyAt,
            LocalDateTime firstReviewAt,
            boolean isApprove
    ) {
        DurationMinutes reviewWait = DurationMinutes.between(reviewReadyAt, firstReviewAt);

        return PullRequestBottleneck.builder()
                .pullRequestId(pullRequestId)
                .reviewWait(reviewWait)
                .reviewProgress(DurationMinutes.zero())
                .firstReviewAt(firstReviewAt)
                .lastReviewAt(firstReviewAt)
                .lastApproveAt(isApprove ? firstReviewAt : null)
                .build();
    }

    private void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("Pull Request ID는 필수입니다.");
        }
    }

    @Builder
    private PullRequestBottleneck(
            Long pullRequestId,
            DurationMinutes reviewWait,
            DurationMinutes reviewProgress,
            DurationMinutes mergeWait,
            LocalDateTime firstReviewAt,
            LocalDateTime lastReviewAt,
            LocalDateTime lastApproveAt
    ) {
        validatePullRequestId(pullRequestId);
        this.pullRequestId = pullRequestId;
        this.reviewWait = reviewWait;
        this.reviewProgress = reviewProgress;
        this.mergeWait = mergeWait;
        this.firstReviewAt = firstReviewAt;
        this.lastReviewAt = lastReviewAt;
        this.lastApproveAt = lastApproveAt;
    }

    public void updateOnNewReview(LocalDateTime reviewSubmittedAt, boolean isApprove) {
        if (this.firstReviewAt == null) {
            this.firstReviewAt = reviewSubmittedAt;
        }

        this.lastReviewAt = reviewSubmittedAt;
        this.reviewProgress = DurationMinutes.between(this.firstReviewAt, reviewSubmittedAt);

        if (isApprove) {
            this.lastApproveAt = reviewSubmittedAt;
        }
    }

    public void updateOnMerge(LocalDateTime mergedAt) {
        if (this.lastApproveAt != null) {
            this.mergeWait = DurationMinutes.between(this.lastApproveAt, mergedAt);
        }
    }

    public boolean hasReview() {
        return firstReviewAt != null;
    }

    public boolean hasReviewWait() {
        return reviewWait != null;
    }

    public boolean hasApproval() {
        return lastApproveAt != null;
    }

    public boolean hasMergeWaitWithApproval() {
        return mergeWait != null && hasApproval();
    }

    public boolean isMerged() {
        return mergeWait != null;
    }

    public DurationMinutes getTotalBottleneckTime() {
        DurationMinutes total = DurationMinutes.zero();

        if (reviewWait != null) {
            total = total.add(reviewWait);
        }
        if (reviewProgress != null) {
            total = total.add(reviewProgress);
        }
        if (mergeWait != null) {
            total = total.add(mergeWait);
        }

        return total;
    }

    public BottleneckType getLongestBottleneck() {
        DurationMinutes maxWait = DurationMinutes.zero();
        BottleneckType longestType = BottleneckType.NONE;

        if (reviewWait != null && reviewWait.isGreaterThan(maxWait)) {
            maxWait = reviewWait;
            longestType = BottleneckType.REVIEW_WAIT;
        }
        if (reviewProgress != null && reviewProgress.isGreaterThan(maxWait)) {
            maxWait = reviewProgress;
            longestType = BottleneckType.REVIEW_PROGRESS;
        }
        if (mergeWait != null && mergeWait.isGreaterThan(maxWait)) {
            longestType = BottleneckType.MERGE_WAIT;
        }

        return longestType;
    }

    public enum BottleneckType {
        NONE,
        REVIEW_WAIT,
        REVIEW_PROGRESS,
        MERGE_WAIT
    }
}
