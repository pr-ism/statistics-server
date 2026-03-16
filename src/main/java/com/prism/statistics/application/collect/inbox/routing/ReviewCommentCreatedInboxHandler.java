package com.prism.statistics.application.collect.inbox.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.analysis.metadata.review.ReviewCommentCreatedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentCreatedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewCommentCreatedInboxHandler implements CollectInboxEventHandler {

    private final ObjectMapper objectMapper;
    private final ReviewCommentCreatedService reviewCommentCreatedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.REVIEW_COMMENT_CREATED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        try {
            ReviewCommentCreatedRequest request = objectMapper.readValue(context.payloadJson(), ReviewCommentCreatedRequest.class);
            reviewCommentCreatedService.createReviewComment(request);
        } catch (Exception e) {
            throw new RuntimeException(supportType() + " 핸들러 처리 중 예외가 발생했습니다.", e);
        }
    }
}
