package com.prism.statistics.application.webhook.dto.request;

import java.time.Instant;

public record ReviewCommentEditedRequest(
        Long githubCommentId,
        String body,
        Instant updatedAt
) {}
