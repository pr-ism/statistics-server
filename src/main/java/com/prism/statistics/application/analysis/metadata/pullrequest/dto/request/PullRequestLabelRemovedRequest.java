package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import com.prism.statistics.application.collect.inbox.CollectInboxRequest;
import java.time.Instant;

public record PullRequestLabelRemovedRequest(
        long runId,
        Long githubPullRequestId,
        int pullRequestNumber,
        String headCommitSha,
        LabelData label,
        Instant unlabeledAt
) implements CollectInboxRequest {

    public record LabelData(
            String name
    ) {}
}
