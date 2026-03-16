package com.prism.statistics.application.collect.inbox.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.analysis.metadata.review.ReviewCommentDeletedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentDeletedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewCommentDeletedInboxHandler implements CollectInboxEventHandler {

    private final ObjectMapper objectMapper;
    private final ReviewCommentDeletedService reviewCommentDeletedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.REVIEW_COMMENT_DELETED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        try {
            ReviewCommentDeletedRequest request = objectMapper.readValue(context.payloadJson(), ReviewCommentDeletedRequest.class);
            reviewCommentDeletedService.deleteReviewComment(request);
        } catch (Exception e) {
            throw new RuntimeException(supportType() + " 핸들러 처리 중 예외가 발생했습니다.", e);
        }
    }
}
