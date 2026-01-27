package com.prism.statistics.application.webhook.event;

import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest;

public record PrDraftCreatedEvent(PrOpenedRequest request) {
}
