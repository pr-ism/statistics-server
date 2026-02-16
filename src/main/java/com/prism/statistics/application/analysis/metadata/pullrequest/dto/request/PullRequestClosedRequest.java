package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import java.time.Instant;

public record PullRequestClosedRequest(
        int pullRequestNumber,
        String headCommitSha,
        boolean isMerged,
        Instant closedAt,
        Instant mergedAt
) {}
