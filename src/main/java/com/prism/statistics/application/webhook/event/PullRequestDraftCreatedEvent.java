package com.prism.statistics.application.webhook.event;

import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest;

public record PullRequestDraftCreatedEvent(PullRequestOpenedRequest request) {
}
