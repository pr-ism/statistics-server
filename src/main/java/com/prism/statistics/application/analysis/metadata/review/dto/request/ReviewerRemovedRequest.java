package com.prism.statistics.application.analysis.metadata.review.dto.request;

import java.time.Instant;

public record ReviewerRemovedRequest(
        int pullRequestNumber,
        ReviewerData reviewer,
        Instant removedAt
) {

    public record ReviewerData(
            String login,
            Long id
    ) {}
}
