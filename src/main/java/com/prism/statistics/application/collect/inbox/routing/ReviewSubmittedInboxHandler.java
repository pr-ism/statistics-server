package com.prism.statistics.application.collect.inbox.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.analysis.metadata.review.ReviewSubmittedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewSubmittedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewSubmittedInboxHandler implements CollectInboxEventHandler {

    private final ObjectMapper objectMapper;
    private final ReviewSubmittedService reviewSubmittedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.REVIEW_SUBMITTED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        try {
            ReviewSubmittedRequest request = objectMapper.readValue(context.payloadJson(), ReviewSubmittedRequest.class);
            reviewSubmittedService.submitReview(request);
        } catch (Exception e) {
            throw new RuntimeException(supportType() + " 핸들러 처리 중 예외가 발생했습니다.", e);
        }
    }
}
