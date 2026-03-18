package com.prism.statistics.application.analysis.metadata.review.dto.request;

import com.prism.statistics.application.collect.inbox.CollectInboxRequest;
import java.time.Instant;

public record ReviewerRemovedRequest(
        long runId,
        Long githubPullRequestId,
        int pullRequestNumber,
        String headCommitSha,
        ReviewerData reviewer,
        Instant removedAt
) implements CollectInboxRequest {

    public record ReviewerData(
            String login,
            Long id
    ) {}
}
