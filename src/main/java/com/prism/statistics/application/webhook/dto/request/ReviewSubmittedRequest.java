package com.prism.statistics.application.webhook.dto.request;

import java.time.Instant;
import java.util.List;

public record ReviewSubmittedRequest(
        int pullRequestNumber,
        Long githubReviewId,
        ReviewerData reviewer,
        String state,
        String commitSha,
        String body,
        Instant submittedAt,
        List<CommentData> comments
) {

    public record ReviewerData(
            String login,
            Long id
    ) {}

    public record CommentData(
            Long githubCommentId,
            Long parentCommentId,
            String body,
            Instant commentedAt
    ) {}
}
