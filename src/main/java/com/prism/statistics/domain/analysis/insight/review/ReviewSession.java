package com.prism.statistics.domain.analysis.insight.review;

import com.prism.statistics.domain.analysis.insight.vo.DurationMinutes;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.common.CreatedAtEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "review_sessions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewSession extends CreatedAtEntity {

    private Long pullRequestId;

    @Embedded
    @AttributeOverride(name = "userName", column = @Column(name = "reviewer_name"))
    @AttributeOverride(name = "userId", column = @Column(name = "reviewer_github_id"))
    private GithubUser reviewer;

    private LocalDateTime firstActivityAt;

    private LocalDateTime lastActivityAt;

    @Embedded
    @AttributeOverride(name = "minutes", column = @Column(name = "session_duration_minutes"))
    private DurationMinutes sessionDuration;

    private int reviewCount;

    private int commentCount;

    public static ReviewSession create(
            Long pullRequestId,
            GithubUser reviewer,
            LocalDateTime firstActivityAt
    ) {
        validatePullRequestId(pullRequestId);
        validateReviewer(reviewer);
        validateActivityAt(firstActivityAt);

        return new ReviewSession(
                pullRequestId, reviewer, firstActivityAt, firstActivityAt,
                DurationMinutes.zero(), 1, 0
        );
    }

    public static ReviewSession createWithComment(
            Long pullRequestId,
            GithubUser reviewer,
            LocalDateTime firstActivityAt,
            int initialCommentCount
    ) {
        validatePullRequestId(pullRequestId);
        validateReviewer(reviewer);
        validateActivityAt(firstActivityAt);

        validateNonNegativeCommentCount(initialCommentCount);

        return new ReviewSession(
                pullRequestId, reviewer, firstActivityAt, firstActivityAt,
                DurationMinutes.zero(), 0, initialCommentCount
        );
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("Pull Request ID는 필수입니다.");
        }
    }

    private static void validateReviewer(GithubUser reviewer) {
        if (reviewer == null) {
            throw new IllegalArgumentException("리뷰어 정보는 필수입니다.");
        }
    }

    private static void validateActivityAt(LocalDateTime activityAt) {
        if (activityAt == null) {
            throw new IllegalArgumentException("활동 시각은 필수입니다.");
        }
    }

    private static void validateNonNegativeCommentCount(int commentCount) {
        if (commentCount < 0) {
            throw new IllegalArgumentException("코멘트 수는 0 이상이어야 합니다.");
        }
    }

    private ReviewSession(
            Long pullRequestId,
            GithubUser reviewer,
            LocalDateTime firstActivityAt,
            LocalDateTime lastActivityAt,
            DurationMinutes sessionDuration,
            int reviewCount,
            int commentCount
    ) {
        this.pullRequestId = pullRequestId;
        this.reviewer = reviewer;
        this.firstActivityAt = firstActivityAt;
        this.lastActivityAt = lastActivityAt;
        this.sessionDuration = sessionDuration;
        this.reviewCount = reviewCount;
        this.commentCount = commentCount;
    }

    public void updateOnReview(LocalDateTime reviewedAt, int newCommentCount) {
        validateActivityAt(reviewedAt);
        validateNonNegativeCommentCount(newCommentCount);
        updateLastActivity(reviewedAt);

        this.reviewCount++;
        this.commentCount += newCommentCount;
    }

    public void updateOnComment(LocalDateTime commentedAt) {
        validateActivityAt(commentedAt);
        updateLastActivity(commentedAt);
        this.commentCount++;
    }

    private void updateLastActivity(LocalDateTime activityAt) {
        if (activityAt.isAfter(this.lastActivityAt)) {
            this.lastActivityAt = activityAt;
            this.sessionDuration = DurationMinutes.between(this.firstActivityAt, activityAt);
        }
    }

    public boolean isSingleActivity() {
        return sessionDuration.isZero();
    }

    public int calculateTotalActivities() {
        return reviewCount + commentCount;
    }

    public boolean isActiveReviewer() {
        return reviewCount > 0;
    }
}
