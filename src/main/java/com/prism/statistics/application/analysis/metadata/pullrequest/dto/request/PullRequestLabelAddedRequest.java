package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

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
