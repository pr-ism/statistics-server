package com.prism.statistics.application.analysis.metadata.review.dto.request;

import com.prism.statistics.application.collect.inbox.CollectInboxRequest;
import java.time.Instant;

public record ReviewCommentEditedRequest(
        long runId,
        Long githubCommentId,
        String body,
        Instant updatedAt
) implements CollectInboxRequest {}
