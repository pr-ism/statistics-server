package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.review.ReviewCommentEditedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentEditedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewCommentEditedInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final ReviewCommentEditedService reviewCommentEditedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.REVIEW_COMMENT_EDITED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        ReviewCommentEditedRequest request = deserializer.deserialize(context, supportType(), ReviewCommentEditedRequest.class);
        reviewCommentEditedService.editReviewComment(request);
    }
}
