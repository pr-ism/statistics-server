package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import com.prism.statistics.application.collect.inbox.CollectInboxRequest;
import java.time.Instant;

public record PullRequestReadyForReviewRequest(
        long runId,
        int pullRequestNumber,
        Instant readyForReviewAt
) implements CollectInboxRequest {}
