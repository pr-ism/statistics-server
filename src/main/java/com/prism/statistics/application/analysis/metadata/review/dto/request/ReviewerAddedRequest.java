package com.prism.statistics.application.analysis.metadata.review.dto.request;

import java.time.Instant;

public record ReviewerAddedRequest(
        int pullRequestNumber,
        ReviewerData reviewer,
        Instant requestedAt
) {

    public record ReviewerData(
            String login,
            Long id
    ) {}
}
