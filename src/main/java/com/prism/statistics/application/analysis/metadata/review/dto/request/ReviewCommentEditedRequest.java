package com.prism.statistics.application.analysis.metadata.review.dto.request;

import java.time.Instant;

public record ReviewCommentEditedRequest(
        long runId,
        Long githubCommentId,
        String body,
        Instant updatedAt
) {}
