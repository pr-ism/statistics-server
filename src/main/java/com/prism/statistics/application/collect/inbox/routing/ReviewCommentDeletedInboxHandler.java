package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.review.ReviewCommentDeletedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentDeletedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewCommentDeletedInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final ReviewCommentDeletedService reviewCommentDeletedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.REVIEW_COMMENT_DELETED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        ReviewCommentDeletedRequest request = deserializer.deserialize(context, supportType(), ReviewCommentDeletedRequest.class);
        reviewCommentDeletedService.deleteReviewComment(request);
    }
}
