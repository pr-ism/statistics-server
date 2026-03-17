package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.review.ReviewCommentCreatedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentCreatedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewCommentCreatedInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final ReviewCommentCreatedService reviewCommentCreatedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.REVIEW_COMMENT_CREATED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        ReviewCommentCreatedRequest request = deserializer.deserialize(context, supportType(), ReviewCommentCreatedRequest.class);
        reviewCommentCreatedService.createReviewComment(request);
    }
}
