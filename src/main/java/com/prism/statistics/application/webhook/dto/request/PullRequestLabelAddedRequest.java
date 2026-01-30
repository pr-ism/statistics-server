package com.prism.statistics.application.webhook.dto.request;

import java.time.Instant;

public record PullRequestLabelAddedRequest(
        int prNumber,
        LabelData label,
        Instant labeledAt
) {

    public record LabelData(
            String name
    ) {}
}
