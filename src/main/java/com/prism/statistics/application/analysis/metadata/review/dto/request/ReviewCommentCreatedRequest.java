package com.prism.statistics.application.analysis.metadata.review.dto.request;

import java.time.Instant;

public record ReviewCommentCreatedRequest(
        Long githubCommentId,
        Long githubReviewId,
        String body,
        String path,
        int line,
        Integer startLine,
        String side,
        String commitSha,
        Long inReplyToId,
        CommentAuthorData author,
        Instant createdAt,
        Instant updatedAt
) {

    public record CommentAuthorData(
            String login,
            Long id
    ) {}
}
