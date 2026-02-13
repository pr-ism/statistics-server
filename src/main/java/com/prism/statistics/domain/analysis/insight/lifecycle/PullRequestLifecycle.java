package com.prism.statistics.domain.analysis.insight.lifecycle;

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
@Table(name = "pull_request_lifecycles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestLifecycle extends BaseTimeEntity {

    private Long pullRequestId;

    private LocalDateTime reviewReadyAt;

    @Embedded
    @AttributeOverride(name = "minutes", column = @Column(name = "time_to_merge_minutes"))
    private DurationMinutes timeToMerge;

    @Embedded
    @AttributeOverride(name = "minutes", column = @Column(name = "total_lifespan_minutes"))
    private DurationMinutes totalLifespan;

    @Embedded
    @AttributeOverride(name = "minutes", column = @Column(name = "active_work_minutes"))
    private DurationMinutes activeWork;

    private int stateChangeCount;

    private boolean reopened;

    private boolean closedWithoutReview;

    public static PullRequestLifecycle createInProgress(
            Long pullRequestId,
            LocalDateTime reviewReadyAt,
            DurationMinutes activeWork,
            int stateChangeCount,
            boolean reopened
    ) {
        return PullRequestLifecycle.builder()
                .pullRequestId(pullRequestId)
                .reviewReadyAt(reviewReadyAt)
                .activeWork(activeWork)
                .stateChangeCount(stateChangeCount)
                .reopened(reopened)
                .build();
    }

    private void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("Pull Request ID는 필수입니다.");
        }
    }

    private void validateReviewReadyAt(LocalDateTime reviewReadyAt) {
        if (reviewReadyAt == null) {
            throw new IllegalArgumentException("리뷰 가능 시점은 필수입니다.");
        }
    }

    private void validateActiveWork(DurationMinutes activeWork) {
        if (activeWork == null) {
            throw new IllegalArgumentException("활성 작업 시간은 필수입니다.");
        }
    }

    private void validateStateChangeCount(int stateChangeCount) {
        if (stateChangeCount < 0) {
            throw new IllegalArgumentException("상태 변경 횟수는 0보다 작을 수 없습니다.");
        }
    }

    @Builder
    private PullRequestLifecycle(
            Long pullRequestId,
            LocalDateTime reviewReadyAt,
            DurationMinutes timeToMerge,
            DurationMinutes totalLifespan,
            DurationMinutes activeWork,
            int stateChangeCount,
            boolean reopened,
            boolean closedWithoutReview
    ) {
        validatePullRequestId(pullRequestId);
        validateReviewReadyAt(reviewReadyAt);
        validateActiveWork(activeWork);
        validateStateChangeCount(stateChangeCount);

        this.pullRequestId = pullRequestId;
        this.reviewReadyAt = reviewReadyAt;
        this.timeToMerge = timeToMerge;
        this.totalLifespan = totalLifespan;
        this.activeWork = activeWork;
        this.stateChangeCount = stateChangeCount;
        this.reopened = reopened;
        this.closedWithoutReview = closedWithoutReview;
    }

    public void updateOnClose(
            DurationMinutes timeToMerge,
            DurationMinutes totalLifespan,
            DurationMinutes finalActiveWork,
            boolean closedWithoutReview
    ) {
        if (totalLifespan == null) {
            throw new IllegalArgumentException("종료 시 실질 작업 기간은 필수입니다.");
        }

        validateActiveWork(finalActiveWork);
        this.timeToMerge = timeToMerge;
        this.totalLifespan = totalLifespan;
        this.activeWork = finalActiveWork;
        this.closedWithoutReview = closedWithoutReview;
    }

    public void updateOnStateChange(
            DurationMinutes currentActiveWork,
            int newStateChangeCount,
            boolean isReopened
    ) {
        validateActiveWork(currentActiveWork);
        validateStateChangeCount(newStateChangeCount);
        this.activeWork = currentActiveWork;
        this.stateChangeCount = newStateChangeCount;
        if (isReopened) {
            this.reopened = true;
        }
    }

    public boolean isMerged() {
        return timeToMerge != null;
    }

    public boolean isClosed() {
        return totalLifespan != null;
    }

    public boolean hasStateChanges() {
        return stateChangeCount > 0;
    }
}
