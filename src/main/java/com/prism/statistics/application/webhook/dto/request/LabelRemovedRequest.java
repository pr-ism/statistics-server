package com.prism.statistics.application.webhook.dto.request;

import java.time.Instant;

public record LabelRemovedRequest(
        int prNumber,
        LabelData label,
        Instant unlabeledAt
) {

    public record LabelData(
            String name
    ) {}
}
