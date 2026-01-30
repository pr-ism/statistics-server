package com.prism.statistics.application.webhook.dto.request;

import java.time.Instant;

public record PullRequestLabelRemovedRequest(
        int prNumber,
        LabelData label,
        Instant unlabeledAt
) {

    public record LabelData(
            String name
    ) {}
}
