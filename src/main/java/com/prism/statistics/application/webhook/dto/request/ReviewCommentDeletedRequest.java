package com.prism.statistics.application.webhook.dto.request;

import java.time.Instant;

public record ReviewCommentDeletedRequest(
        Long githubCommentId,
        Instant updatedAt
) {}
