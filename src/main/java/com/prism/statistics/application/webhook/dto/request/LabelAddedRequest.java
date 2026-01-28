package com.prism.statistics.application.webhook.dto.request;

import java.time.Instant;

public record LabelAddedRequest(
        String repositoryFullName,
        int prNumber,
        LabelData label,
        Instant labeledAt
) {

    public record LabelData(
            String name
    ) {}
}
