package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import java.time.Instant;

public record PullRequestReadyForReviewRequest(
        long runId,
        int pullRequestNumber,
        Instant readyForReviewAt
) {}
