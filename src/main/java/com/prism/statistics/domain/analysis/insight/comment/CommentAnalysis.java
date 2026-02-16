package com.prism.statistics.domain.analysis.insight.comment;

import com.prism.statistics.domain.analysis.insight.comment.vo.MentionedUsers;
import com.prism.statistics.domain.common.CreatedAtEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "comment_analyses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentAnalysis extends CreatedAtEntity {

    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`[^`]+`");
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[^\\s]+");

    private Long reviewCommentId;

    private Long pullRequestId;

    private int commentLength;

    private int lineCount;

    @Embedded
    private MentionedUsers mentionedUsers;

    private boolean hasCode;

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
        boolean hasCode = containsCode(body);
        boolean hasUrl = containsUrl(body);

        return new CommentAnalysis(
                reviewCommentId,
                pullRequestId,
                commentLength,
                lineCount,
                mentionedUsers,
                hasCode,
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

    private static boolean containsCode(String body) {
        if (body == null) {
            return false;
        }
        return body.contains("```") || INLINE_CODE_PATTERN.matcher(body).find();
    }

    private static boolean containsUrl(String body) {
        if (body == null) {
            return false;
        }
        return URL_PATTERN.matcher(body).find();
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
            boolean hasCode,
            boolean hasUrl
    ) {
        this.reviewCommentId = reviewCommentId;
        this.pullRequestId = pullRequestId;
        this.commentLength = commentLength;
        this.lineCount = lineCount;
        this.mentionedUsers = mentionedUsers;
        this.hasCode = hasCode;
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
        return hasCode || hasUrl;
    }
}
