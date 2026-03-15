package com.prism.statistics.application.analysis.metadata.review.dto.request;

import java.time.Instant;

public record ReviewCommentDeletedRequest(
        Long runId,
        Long githubCommentId,
        Instant updatedAt
) {}
