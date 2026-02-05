package com.prism.statistics.application.analysis.metadata.pullrequest.event;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest;

public record PullRequestDraftCreatedEvent(PullRequestOpenedRequest request) {
}
