package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import java.time.Instant;

public record PullRequestConvertedToDraftRequest(
        long runId,
        int pullRequestNumber,
        Instant convertedToDraftAt
) {}
