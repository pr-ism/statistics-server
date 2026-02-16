package com.prism.statistics.domain.analysis.insight.comment;

import com.prism.statistics.domain.analysis.insight.comment.vo.MentionedUsers;
import com.prism.statistics.domain.common.CreatedAtEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "comment_analyses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentAnalysis extends CreatedAtEntity {

    private Long reviewCommentId;

    private Long pullRequestId;

    private int commentLength;

    private int lineCount;

    @Embedded
    private MentionedUsers mentionedUsers;

    private boolean hasCodeBlock;

    private boolean hasUrl;

    public static CommentAnalysis create(
            Long reviewCommentId,
            Long pullRequestId,
            String body
    ) {
        validateReviewCommentId(reviewCommentId);
        validatePullRequestId(pullRequestId);

        int commentLength = calculateLength(body);
        int lineCount = calculateLineCount(body);
        MentionedUsers mentionedUsers = MentionedUsers.fromBody(body);
        boolean hasCodeBlock = containsCodeBlock(body);
        boolean hasUrl = containsUrl(body);

        return new CommentAnalysis(
                reviewCommentId,
                pullRequestId,
                commentLength,
                lineCount,
                mentionedUsers,
                hasCodeBlock,
                hasUrl
        );
    }

    private static int calculateLength(String body) {
        if (body == null) {
            return 0;
        }
        return body.length();
    }

    private static int calculateLineCount(String body) {
        if (body == null || body.isEmpty()) {
            return 0;
        }
        return body.split("\r?\n").length;
    }

    private static boolean containsCodeBlock(String body) {
        if (body == null) {
            return false;
        }
        return body.contains("```") || body.matches(".*`[^`]+`.*");
    }

    private static boolean containsUrl(String body) {
        if (body == null) {
            return false;
        }
        return body.matches(".*https?://[^\\s]+.*");
    }

    private static void validateReviewCommentId(Long reviewCommentId) {
        if (reviewCommentId == null) {
            throw new IllegalArgumentException("Review Comment ID는 필수입니다.");
        }
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("Pull Request ID는 필수입니다.");
        }
    }

    private CommentAnalysis(
            Long reviewCommentId,
            Long pullRequestId,
            int commentLength,
            int lineCount,
            MentionedUsers mentionedUsers,
            boolean hasCodeBlock,
            boolean hasUrl
    ) {
        this.reviewCommentId = reviewCommentId;
        this.pullRequestId = pullRequestId;
        this.commentLength = commentLength;
        this.lineCount = lineCount;
        this.mentionedUsers = mentionedUsers;
        this.hasCodeBlock = hasCodeBlock;
        this.hasUrl = hasUrl;
    }

    public boolean hasMentions() {
        return mentionedUsers != null && !mentionedUsers.isEmpty();
    }

    public int getMentionCount() {
        return mentionedUsers != null ? mentionedUsers.getCount() : 0;
    }

    public boolean isShortComment() {
        return commentLength < 50;
    }

    public boolean isDetailedComment() {
        return commentLength >= 200;
    }

    public boolean isRichComment() {
        return hasCodeBlock || hasUrl;
    }
}
