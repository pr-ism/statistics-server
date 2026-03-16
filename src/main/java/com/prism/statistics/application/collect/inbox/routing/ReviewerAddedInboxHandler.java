package com.prism.statistics.application.collect.inbox.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.analysis.metadata.review.ReviewerAddedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewerAddedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewerAddedInboxHandler implements CollectInboxEventHandler {

    private final ObjectMapper objectMapper;
    private final ReviewerAddedService reviewerAddedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.REVIEWER_ADDED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        try {
            ReviewerAddedRequest request = objectMapper.readValue(context.payloadJson(), ReviewerAddedRequest.class);
            reviewerAddedService.addReviewer(request);
        } catch (Exception e) {
            throw new RuntimeException(supportType() + " 핸들러 처리 중 예외가 발생했습니다.", e);
        }
    }
}
