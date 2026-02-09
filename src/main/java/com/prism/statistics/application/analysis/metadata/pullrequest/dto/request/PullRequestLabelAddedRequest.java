package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import java.time.Instant;

public record PullRequestLabelAddedRequest(
        Long githubPullRequestId,
        int pullRequestNumber,
        String headCommitSha,
        LabelData label,
        Instant labeledAt
) {

    public record LabelData(
            String name
    ) {}
}
