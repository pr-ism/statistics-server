package com.prism.statistics.application.analysis.metadata.review.dto.request;

import com.prism.statistics.application.collect.inbox.CollectInboxRequest;
import java.time.Instant;

public record ReviewCommentCreatedRequest(
        long runId,
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
        Instant createdAt
) implements CollectInboxRequest {

    public record CommentAuthorData(
            String login,
            Long id
    ) {}
}
