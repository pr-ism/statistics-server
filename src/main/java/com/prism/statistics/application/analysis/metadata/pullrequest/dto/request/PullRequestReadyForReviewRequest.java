package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import java.time.Instant;

public record PullRequestReadyForReviewRequest(
        Long runId,
        int pullRequestNumber,
        Instant readyForReviewAt
) {}
