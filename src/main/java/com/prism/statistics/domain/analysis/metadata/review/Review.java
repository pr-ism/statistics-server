package com.prism.statistics.domain.analysis.metadata.review;

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

    private Long githubPullRequestId;

    private Long githubReviewId;

    private String githubMention;

    private Long githubUid;

    @Enumerated(EnumType.STRING)
    private ReviewState reviewState;

    private String commitSha;

    @Embedded
    private ReviewBody body;

    private int commentCount;

    private LocalDateTime submittedAt;

    public static Review create(
            Long githubPullRequestId,
            Long githubReviewId,
            String githubMention,
            Long githubUid,
            ReviewState reviewState,
            String commitSha,
            String body,
            int commentCount,
            LocalDateTime submittedAt
    ) {
        return Review.builder()
                .githubPullRequestId(githubPullRequestId)
                .githubReviewId(githubReviewId)
                .githubMention(githubMention)
                .githubUid(githubUid)
                .reviewState(reviewState)
                .commitSha(commitSha)
                .body(createReviewBody(reviewState, body))
                .commentCount(commentCount)
                .submittedAt(submittedAt)
                .build();
    }

    private static ReviewBody createReviewBody(ReviewState reviewState, String body) {
        if (reviewState == ReviewState.COMMENTED) {
            return ReviewBody.createRequired(body);
        }
        return ReviewBody.create(body);
    }

    @Builder
    private Review(
            Long githubPullRequestId,
            Long githubReviewId,
            String githubMention,
            Long githubUid,
            ReviewState reviewState,
            String commitSha,
            ReviewBody body,
            int commentCount,
            LocalDateTime submittedAt
    ) {
        validateFields(githubPullRequestId, githubReviewId, githubMention, githubUid, reviewState, commitSha, commentCount, submittedAt);

        this.githubPullRequestId = githubPullRequestId;
        this.githubReviewId = githubReviewId;
        this.githubMention = githubMention;
        this.githubUid = githubUid;
        this.reviewState = reviewState;
        this.commitSha = commitSha;
        this.body = body;
        this.commentCount = commentCount;
        this.submittedAt = submittedAt;
    }

    private void validateFields(
            Long githubPullRequestId,
            Long githubReviewId,
            String githubMention,
            Long githubUid,
            ReviewState reviewState,
            String commitSha,
            int commentCount,
            LocalDateTime submittedAt
    ) {
        validateGithubPullRequestId(githubPullRequestId);
        validateGithubReviewId(githubReviewId);
        validateGithubMention(githubMention);
        validateGithubUid(githubUid);
        validateReviewState(reviewState);
        validateCommitSha(commitSha);
        validateCommentCount(commentCount);
        validateSubmittedAt(submittedAt);
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

    private void validateGithubMention(String githubMention) {
        if (githubMention == null || githubMention.isBlank()) {
            throw new IllegalArgumentException("GitHub 멘션은 필수입니다.");
        }
    }

    private void validateGithubUid(Long githubUid) {
        if (githubUid == null) {
            throw new IllegalArgumentException("GitHub UID는 필수입니다.");
        }
    }

    private void validateReviewState(ReviewState reviewState) {
        if (reviewState == null) {
            throw new IllegalArgumentException("리뷰 상태는 필수입니다.");
        }
    }

    private void validateCommitSha(String commitSha) {
        if (commitSha == null || commitSha.isBlank()) {
            throw new IllegalArgumentException("커밋 SHA는 필수입니다.");
        }
    }

    private void validateCommentCount(int commentCount) {
        if (commentCount < 0) {
            throw new IllegalArgumentException("댓글 수는 0 이상이어야 합니다.");
        }
    }

    private void validateSubmittedAt(LocalDateTime submittedAt) {
        if (submittedAt == null) {
            throw new IllegalArgumentException("리뷰 제출 시각은 필수입니다.");
        }
    }
}
