package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import java.time.Instant;

public record PullRequestReopenedRequest(
        Long runId,
        int pullRequestNumber,
        Instant reopenedAt
) {}
