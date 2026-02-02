package com.prism.statistics.application.webhook.dto.request;

import java.time.Instant;

public record ReviewSubmittedRequest(
        Long githubPullRequestId,
        int pullRequestNumber,
        Long githubReviewId,
        ReviewerData reviewer,
        String state,
        String commitSha,
        String body,
        int commentCount,
        Instant submittedAt
) {

    public record ReviewerData(
            String login,
            Long id
    ) {}
}
