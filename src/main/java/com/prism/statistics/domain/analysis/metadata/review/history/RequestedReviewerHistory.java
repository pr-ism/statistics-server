package com.prism.statistics.domain.analysis.metadata.review.history;

import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewerAction;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "requested_reviewer_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequestedReviewerHistory extends CreatedAtEntity {

    private Long pullRequestId;

    private Long githubPullRequestId;

    private String headCommitSha;

    @Embedded
    private GithubUser reviewer;

    @Enumerated(EnumType.STRING)
    private ReviewerAction action;

    private LocalDateTime changedAt;

    public static RequestedReviewerHistory create(
            Long githubPullRequestId,
            String headCommitSha,
            GithubUser reviewer,
            ReviewerAction action,
            LocalDateTime changedAt
    ) {
        return new RequestedReviewerHistory(
                githubPullRequestId,
                headCommitSha,
                reviewer,
                action,
                changedAt
        );
    }

    @Builder
    private RequestedReviewerHistory(
            Long githubPullRequestId,
            String headCommitSha,
            GithubUser reviewer,
            ReviewerAction action,
            LocalDateTime changedAt
    ) {
        validateFields(githubPullRequestId, headCommitSha, reviewer, action, changedAt);

        this.githubPullRequestId = githubPullRequestId;
        this.headCommitSha = headCommitSha;
        this.reviewer = reviewer;
        this.action = action;
        this.changedAt = changedAt;
    }

    private static void validateFields(
            Long githubPullRequestId,
            String headCommitSha,
            GithubUser reviewer,
            ReviewerAction action,
            LocalDateTime changedAt
    ) {
        validateGithubPullRequestId(githubPullRequestId);
        validateHeadCommitSha(headCommitSha);
        validateReviewer(reviewer);
        validateAction(action);
        validateChangedAt(changedAt);
    }

    private static void validateGithubPullRequestId(Long githubPullRequestId) {
        if (githubPullRequestId == null) {
            throw new IllegalArgumentException("GitHub PullRequest ID는 필수입니다.");
        }
    }

    private static void validateHeadCommitSha(String headCommitSha) {
        if (headCommitSha == null || headCommitSha.isBlank()) {
            throw new IllegalArgumentException("Head Commit SHA는 필수입니다.");
        }
    }

    private static void validateReviewer(GithubUser reviewer) {
        if (reviewer == null) {
            throw new IllegalArgumentException("리뷰어는 필수입니다.");
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
}
