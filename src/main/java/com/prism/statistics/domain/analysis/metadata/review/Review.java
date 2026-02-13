package com.prism.statistics.domain.analysis.metadata.review;

import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewState;
import com.prism.statistics.domain.analysis.metadata.review.vo.ReviewBody;
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
@Table(name = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends CreatedAtEntity {

    private Long pullRequestId;

    private Long githubPullRequestId;

    private Long githubReviewId;

    @Embedded
    private GithubUser reviewer;

    @Enumerated(EnumType.STRING)
    private ReviewState reviewState;

    private String headCommitSha;

    @Embedded
    private ReviewBody body;

    private int commentCount;

    private LocalDateTime githubSubmittedAt;

    public void assignPullRequestId(Long pullRequestId) {
        if (this.pullRequestId == null) {
            this.pullRequestId = pullRequestId;
        }
    }

    @Builder
    private Review(
            Long githubPullRequestId,
            Long githubReviewId,
            GithubUser reviewer,
            ReviewState reviewState,
            String headCommitSha,
            String body,
            int commentCount,
            LocalDateTime githubSubmittedAt
    ) {
        validateFields(githubPullRequestId, githubReviewId, reviewer, reviewState, headCommitSha, commentCount, githubSubmittedAt);

        this.githubPullRequestId = githubPullRequestId;
        this.githubReviewId = githubReviewId;
        this.reviewer = reviewer;
        this.reviewState = reviewState;
        this.headCommitSha = headCommitSha;
        this.body = createReviewBody(reviewState, body);
        this.commentCount = commentCount;
        this.githubSubmittedAt = githubSubmittedAt;
    }

    private static ReviewBody createReviewBody(ReviewState reviewState, String body) {
        if (reviewState == ReviewState.COMMENTED) {
            return ReviewBody.createRequired(body);
        }
        return ReviewBody.create(body);
    }

    private void validateFields(
            Long githubPullRequestId,
            Long githubReviewId,
            GithubUser reviewer,
            ReviewState reviewState,
            String headCommitSha,
            int commentCount,
            LocalDateTime githubSubmittedAt
    ) {
        validateGithubPullRequestId(githubPullRequestId);
        validateGithubReviewId(githubReviewId);
        validateReviewer(reviewer);
        validateReviewState(reviewState);
        validateHeadCommitSha(headCommitSha);
        validateCommentCount(commentCount);
        validateSubmittedAt(githubSubmittedAt);
    }

    private void validateGithubPullRequestId(Long githubPullRequestId) {
        if (githubPullRequestId == null) {
            throw new IllegalArgumentException("GitHub PullRequest ID는 필수입니다.");
        }
    }

    private void validateGithubReviewId(Long githubReviewId) {
        if (githubReviewId == null) {
            throw new IllegalArgumentException("GitHub Review ID는 필수입니다.");
        }
    }

    private void validateReviewer(GithubUser reviewer) {
        if (reviewer == null) {
            throw new IllegalArgumentException("리뷰어는 필수입니다.");
        }
    }

    private void validateReviewState(ReviewState reviewState) {
        if (reviewState == null) {
            throw new IllegalArgumentException("리뷰 상태는 필수입니다.");
        }
    }

    private void validateHeadCommitSha(String headCommitSha) {
        if (headCommitSha == null || headCommitSha.isBlank()) {
            throw new IllegalArgumentException("헤드 커밋 SHA는 필수입니다.");
        }
    }

    private void validateCommentCount(int commentCount) {
        if (commentCount < 0) {
            throw new IllegalArgumentException("댓글 수는 0 이상이어야 합니다.");
        }
    }

    private void validateSubmittedAt(LocalDateTime githubSubmittedAt) {
        if (githubSubmittedAt == null) {
            throw new IllegalArgumentException("리뷰 제출 시각은 필수입니다.");
        }
    }
}
