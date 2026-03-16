package com.prism.statistics.application.collect.inbox.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.analysis.metadata.review.ReviewerRemovedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewerRemovedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewerRemovedInboxHandler implements CollectInboxEventHandler {

    private final ObjectMapper objectMapper;
    private final ReviewerRemovedService reviewerRemovedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.REVIEWER_REMOVED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        try {
            ReviewerRemovedRequest request = objectMapper.readValue(context.payloadJson(), ReviewerRemovedRequest.class);
            reviewerRemovedService.removeReviewer(request);
        } catch (Exception e) {
            throw new RuntimeException(supportType() + " 핸들러 처리 중 예외가 발생했습니다.", e);
        }
    }
}
