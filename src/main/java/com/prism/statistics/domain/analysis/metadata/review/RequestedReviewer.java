
package com.prism.statistics.domain.analysis.metadata.review;

import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.common.CreatedAtEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "requested_reviewers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequestedReviewer extends CreatedAtEntity {

    private Long pullRequestId;

    private Long githubPullRequestId;

    private String headCommitSha;

    @Embedded
    private GithubUser reviewer;

    private LocalDateTime requestedAt;

    public static RequestedReviewer create(
            Long githubPullRequestId,
            String headCommitSha,
            GithubUser reviewer,
            LocalDateTime requestedAt
    ) {
        validateGithubPullRequestId(githubPullRequestId);
        validateHeadCommitSha(headCommitSha);
        validateReviewer(reviewer);
        validateRequestedAt(requestedAt);
        return new RequestedReviewer(githubPullRequestId, headCommitSha, reviewer, requestedAt);
    }

    private static void validateGithubPullRequestId(Long githubPullRequestId) {
        if (githubPullRequestId == null) {
            throw new IllegalArgumentException("Github PullRequest ID는 필수입니다.");
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

    private static void validateRequestedAt(LocalDateTime requestedAt) {
        if (requestedAt == null) {
            throw new IllegalArgumentException("리뷰어 요청 시각은 필수입니다.");
        }
    }

    private RequestedReviewer(
            Long githubPullRequestId,
            String headCommitSha,
            GithubUser reviewer,
            LocalDateTime requestedAt
    ) {
        this.githubPullRequestId = githubPullRequestId;
        this.headCommitSha = headCommitSha;
        this.reviewer = reviewer;
        this.requestedAt = requestedAt;
    }
}
