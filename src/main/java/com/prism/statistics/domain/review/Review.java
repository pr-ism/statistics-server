package com.prism.statistics.domain.review;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.review.enums.ReviewState;
import com.prism.statistics.domain.review.vo.ReviewBody;
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

    public static Review createApproved(
            Long githubPullRequestId,
            Long githubReviewId,
            String githubMention,
            Long githubUid,
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
                .reviewState(ReviewState.APPROVED)
                .commitSha(commitSha)
                .body(ReviewBody.create(body))
                .commentCount(commentCount)
                .submittedAt(submittedAt)
                .build();
    }

    public static Review createChangesRequested(
            Long githubPullRequestId,
            Long githubReviewId,
            String githubMention,
            Long githubUid,
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
                .reviewState(ReviewState.CHANGES_REQUESTED)
                .commitSha(commitSha)
                .body(ReviewBody.create(body))
                .commentCount(commentCount)
                .submittedAt(submittedAt)
                .build();
    }

    public static Review createCommented(
            Long githubPullRequestId,
            Long githubReviewId,
            String githubMention,
            Long githubUid,
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
                .reviewState(ReviewState.COMMENTED)
                .commitSha(commitSha)
                .body(ReviewBody.createRequired(body))
                .commentCount(commentCount)
                .submittedAt(submittedAt)
                .build();
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
        validateRequiredFields(githubPullRequestId, githubReviewId, githubMention, githubUid, reviewState, commitSha, submittedAt);

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

    private static void validateRequiredFields(
            Long githubPullRequestId,
            Long githubReviewId,
            String githubMention,
            Long githubUid,
            ReviewState reviewState,
            String commitSha,
            LocalDateTime submittedAt
    ) {
        if (githubPullRequestId == null) {
            throw new IllegalArgumentException("GitHub PullRequest ID는 필수입니다.");
        }
        if (githubReviewId == null) {
            throw new IllegalArgumentException("GitHub Review ID는 필수입니다.");
        }
        if (githubMention == null || githubMention.isBlank()) {
            throw new IllegalArgumentException("GitHub 멘션은 필수입니다.");
        }
        if (githubUid == null) {
            throw new IllegalArgumentException("GitHub UID는 필수입니다.");
        }
        if (reviewState == null) {
            throw new IllegalArgumentException("리뷰 상태는 필수입니다.");
        }
        if (commitSha == null || commitSha.isBlank()) {
            throw new IllegalArgumentException("커밋 SHA는 필수입니다.");
        }
        if (submittedAt == null) {
            throw new IllegalArgumentException("리뷰 제출 시각은 필수입니다.");
        }
    }
}
