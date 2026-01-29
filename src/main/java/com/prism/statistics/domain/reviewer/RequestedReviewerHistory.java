package com.prism.statistics.domain.reviewer;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.reviewer.enums.ReviewerAction;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "requested_reviewer_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequestedReviewerHistory extends CreatedAtEntity {

    private Long pullRequestId;

    private String reviewerUsername;

    private Long reviewerGithubId;

    @Enumerated(EnumType.STRING)
    private ReviewerAction action;

    private LocalDateTime changedAt;

    public static RequestedReviewerHistory create(
            Long pullRequestId,
            String reviewerUsername,
            Long reviewerGithubId,
            ReviewerAction action,
            LocalDateTime changedAt
    ) {
        validatePullRequestId(pullRequestId);
        validateReviewerUsername(reviewerUsername);
        validateReviewerGithubId(reviewerGithubId);
        validateAction(action);
        validateChangedAt(changedAt);
        return new RequestedReviewerHistory(pullRequestId, reviewerUsername, reviewerGithubId, action, changedAt);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("PR ID는 필수입니다.");
        }
    }

    private static void validateReviewerUsername(String reviewerUsername) {
        if (reviewerUsername == null || reviewerUsername.isBlank()) {
            throw new IllegalArgumentException("리뷰어 사용자명은 필수입니다.");
        }
    }

    private static void validateReviewerGithubId(Long reviewerGithubId) {
        if (reviewerGithubId == null) {
            throw new IllegalArgumentException("리뷰어 GitHub ID는 필수입니다.");
        }
    }

    private static void validateAction(ReviewerAction action) {
        if (action == null) {
            throw new IllegalArgumentException("리뷰어 액션은 필수입니다.");
        }
    }

    private static void validateChangedAt(LocalDateTime changedAt) {
        if (changedAt == null) {
            throw new IllegalArgumentException("변경 시각은 필수입니다.");
        }
    }

    private RequestedReviewerHistory(
            Long pullRequestId,
            String reviewerUsername,
            Long reviewerGithubId,
            ReviewerAction action,
            LocalDateTime changedAt
    ) {
        this.pullRequestId = pullRequestId;
        this.reviewerUsername = reviewerUsername;
        this.reviewerGithubId = reviewerGithubId;
        this.action = action;
        this.changedAt = changedAt;
    }
}
