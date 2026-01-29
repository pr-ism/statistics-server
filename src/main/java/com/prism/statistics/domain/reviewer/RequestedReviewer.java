
package com.prism.statistics.domain.reviewer;

import com.prism.statistics.domain.common.CreatedAtEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "reviewer_assignments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequestedReviewer extends CreatedAtEntity {

    private Long pullRequestId;

    private String reviewerUsername;

    private Long reviewerGithubId;

    private LocalDateTime requestedAt;

    public static RequestedReviewer create(
            Long pullRequestId,
            String reviewerUsername,
            Long reviewerGithubId,
            LocalDateTime requestedAt
    ) {
        validatePullRequestId(pullRequestId);
        validateReviewerUsername(reviewerUsername);
        validateReviewerGithubId(reviewerGithubId);
        validateRequestedAt(requestedAt);
        return new RequestedReviewer(pullRequestId, reviewerUsername, reviewerGithubId, requestedAt);
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

    private static void validateRequestedAt(LocalDateTime requestedAt) {
        if (requestedAt == null) {
            throw new IllegalArgumentException("리뷰어 요청 시각은 필수입니다.");
        }
    }

    private RequestedReviewer(
            Long pullRequestId,
            String reviewerUsername,
            Long reviewerGithubId,
            LocalDateTime requestedAt
    ) {
        this.pullRequestId = pullRequestId;
        this.reviewerUsername = reviewerUsername;
        this.reviewerGithubId = reviewerGithubId;
        this.requestedAt = requestedAt;
    }
}
