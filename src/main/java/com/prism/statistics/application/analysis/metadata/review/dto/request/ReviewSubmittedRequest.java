package com.prism.statistics.application.analysis.metadata.review.dto.request;

import com.prism.statistics.application.collect.inbox.CollectInboxRequest;
import java.time.Instant;

public record ReviewSubmittedRequest(
        long runId,
        Long githubPullRequestId,
        int pullRequestNumber,
        Long githubReviewId,
        ReviewerData reviewer,
        String state,
        String commitSha,
        String body,
        int commentCount,
        Instant submittedAt
) implements CollectInboxRequest {

    public record ReviewerData(
            String login,
            Long id
    ) {}
}
