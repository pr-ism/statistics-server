package com.prism.statistics.domain.reviewcomment;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.reviewcomment.enums.CommentSide;
import com.prism.statistics.domain.reviewcomment.vo.CommentLineRange;
import com.prism.statistics.domain.reviewcomment.vo.ParentCommentId;
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
@Table(name = "review_comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewComment extends CreatedAtEntity {

    private Long githubCommentId;

    private Long githubReviewId;

    private String body;

    private String path;

    @Embedded
    private CommentLineRange lineRange;

    @Enumerated(EnumType.STRING)
    private CommentSide side;

    private String commitSha;

    @Embedded
    private ParentCommentId parentCommentId;

    private String authorMention;

    private Long authorGithubUid;

    private LocalDateTime githubCreatedAt;

    private LocalDateTime githubUpdatedAt;

    private boolean deleted;

    public static ReviewComment create(
            Long githubCommentId,
            Long githubReviewId,
            String body,
            String path,
            Integer startLine,
            int endLine,
            String side,
            String commitSha,
            Long inReplyToId,
            String authorMention,
            Long authorGithubUid,
            LocalDateTime githubCreatedAt,
            LocalDateTime githubUpdatedAt
    ) {
        return ReviewComment.builder()
                .githubCommentId(githubCommentId)
                .githubReviewId(githubReviewId)
                .body(body)
                .path(path)
                .lineRange(CommentLineRange.create(startLine, endLine))
                .side(CommentSide.from(side))
                .commitSha(commitSha)
                .parentCommentId(ParentCommentId.create(inReplyToId))
                .authorMention(authorMention)
                .authorGithubUid(authorGithubUid)
                .githubCreatedAt(githubCreatedAt)
                .githubUpdatedAt(githubUpdatedAt)
                .deleted(false)
                .build();
    }

    @Builder
    private ReviewComment(
            Long githubCommentId,
            Long githubReviewId,
            String body,
            String path,
            CommentLineRange lineRange,
            CommentSide side,
            String commitSha,
            ParentCommentId parentCommentId,
            String authorMention,
            Long authorGithubUid,
            LocalDateTime githubCreatedAt,
            LocalDateTime githubUpdatedAt,
            boolean deleted
    ) {
        validateFields(githubCommentId, githubReviewId, body, path, lineRange, side, commitSha, authorMention, authorGithubUid, githubCreatedAt, githubUpdatedAt);

        this.githubCommentId = githubCommentId;
        this.githubReviewId = githubReviewId;
        this.body = body;
        this.path = path;
        this.lineRange = lineRange;
        this.side = side;
        this.commitSha = commitSha;
        this.parentCommentId = parentCommentId;
        this.authorMention = authorMention;
        this.authorGithubUid = authorGithubUid;
        this.githubCreatedAt = githubCreatedAt;
        this.githubUpdatedAt = githubUpdatedAt;
        this.deleted = deleted;
    }

    private void validateFields(
            Long githubCommentId,
            Long githubReviewId,
            String body,
            String path,
            CommentLineRange lineRange,
            CommentSide side,
            String commitSha,
            String authorMention,
            Long authorGithubUid,
            LocalDateTime githubCreatedAt,
            LocalDateTime githubUpdatedAt
    ) {
        validateGithubCommentId(githubCommentId);
        validateGithubReviewId(githubReviewId);
        validateBody(body);
        validatePath(path);
        validateLineRange(lineRange);
        validateSide(side);
        validateCommitSha(commitSha);
        validateAuthorMention(authorMention);
        validateAuthorGithubUid(authorGithubUid);
        validateGithubCreatedAt(githubCreatedAt);
        validateGithubUpdatedAt(githubUpdatedAt);
        validateCreatedAtEqualsUpdatedAt(githubCreatedAt, githubUpdatedAt);
    }

    private void validateGithubCommentId(Long githubCommentId) {
        if (githubCommentId == null) {
            throw new IllegalArgumentException("GitHub Comment ID는 필수입니다.");
        }
    }

    private void validateGithubReviewId(Long githubReviewId) {
        if (githubReviewId == null) {
            throw new IllegalArgumentException("GitHub Review ID는 필수입니다.");
        }
    }

    private void validateBody(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }
    }

    private void validatePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("파일 경로는 필수입니다.");
        }
    }

    private void validateLineRange(CommentLineRange lineRange) {
        if (lineRange == null) {
            throw new IllegalArgumentException("라인 범위는 필수입니다.");
        }
    }

    private void validateSide(CommentSide side) {
        if (side == null) {
            throw new IllegalArgumentException("CommentSide는 필수입니다.");
        }
    }

    private void validateCommitSha(String commitSha) {
        if (commitSha == null || commitSha.isBlank()) {
            throw new IllegalArgumentException("커밋 SHA는 필수입니다.");
        }
    }

    private void validateAuthorMention(String authorMention) {
        if (authorMention == null || authorMention.isBlank()) {
            throw new IllegalArgumentException("작성자 멘션은 필수입니다.");
        }
    }

    private void validateAuthorGithubUid(Long authorGithubUid) {
        if (authorGithubUid == null) {
            throw new IllegalArgumentException("작성자 GitHub UID는 필수입니다.");
        }
    }

    private void validateGithubCreatedAt(LocalDateTime githubCreatedAt) {
        if (githubCreatedAt == null) {
            throw new IllegalArgumentException("GitHub 생성 시각은 필수입니다.");
        }
    }

    private void validateGithubUpdatedAt(LocalDateTime githubUpdatedAt) {
        if (githubUpdatedAt == null) {
            throw new IllegalArgumentException("GitHub 수정 시각은 필수입니다.");
        }
    }

    private void validateCreatedAtEqualsUpdatedAt(LocalDateTime githubCreatedAt, LocalDateTime githubUpdatedAt) {
        if (!githubCreatedAt.equals(githubUpdatedAt)) {
            throw new IllegalArgumentException("생성 시 GitHub 생성 시각과 수정 시각은 동일해야 합니다.");
        }
    }

    public boolean isOlderThan(LocalDateTime eventTime) {
        return this.githubUpdatedAt == null || eventTime.isAfter(this.githubUpdatedAt);
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public ParentCommentId getParentCommentId() {
        if (parentCommentId == null) {
            return ParentCommentId.empty();
        }
        return parentCommentId;
    }
}
