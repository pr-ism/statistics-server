package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import java.time.Instant;

public record PullRequestReadyForReviewRequest(
        int pullRequestNumber,
        Instant readyForReviewAt
) {}
