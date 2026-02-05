package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import java.time.Instant;

public record PullRequestLabelRemovedRequest(
        int pullRequestNumber,
        LabelData label,
        Instant unlabeledAt
) {

    public record LabelData(
            String name
    ) {}
}
