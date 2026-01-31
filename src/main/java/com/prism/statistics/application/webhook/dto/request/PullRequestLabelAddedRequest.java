package com.prism.statistics.application.webhook.dto.request;

import java.time.Instant;

public record PullRequestLabelAddedRequest(
        int pullRequestNumber,
        LabelData label,
        Instant labeledAt
) {

    public record LabelData(
            String name
    ) {}
}
