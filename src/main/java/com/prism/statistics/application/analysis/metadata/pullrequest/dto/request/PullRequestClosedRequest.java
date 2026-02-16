package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import java.time.Instant;

public record PullRequestClosedRequest(
        int pullRequestNumber,
        boolean isMerged,
        Instant closedAt,
        Instant mergedAt
) {}
