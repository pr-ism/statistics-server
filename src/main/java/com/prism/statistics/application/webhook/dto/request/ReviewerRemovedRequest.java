package com.prism.statistics.application.webhook.dto.request;

import java.time.Instant;

public record ReviewerRemovedRequest(
        int prNumber,
        ReviewerData reviewer,
        Instant removedAt
) {

    public record ReviewerData(
            String login,
            Long id
    ) {}
}
