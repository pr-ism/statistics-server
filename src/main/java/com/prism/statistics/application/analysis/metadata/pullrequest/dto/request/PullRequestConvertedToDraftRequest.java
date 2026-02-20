package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import java.time.Instant;

public record PullRequestConvertedToDraftRequest(
        int pullRequestNumber,
        Instant convertedToDraftAt
) {}
