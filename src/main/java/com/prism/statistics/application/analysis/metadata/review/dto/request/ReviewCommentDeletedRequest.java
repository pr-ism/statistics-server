package com.prism.statistics.application.analysis.metadata.review.dto.request;

import java.time.Instant;

public record ReviewCommentDeletedRequest(
        Long githubCommentId,
        Instant updatedAt
) {}
