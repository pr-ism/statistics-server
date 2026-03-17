package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import java.time.Instant;

public record PullRequestLabelRemovedRequest(
        long runId,
        Long githubPullRequestId,
        int pullRequestNumber,
        String headCommitSha,
        LabelData label,
        Instant unlabeledAt
) {

    public record LabelData(
            String name
    ) {}
}
