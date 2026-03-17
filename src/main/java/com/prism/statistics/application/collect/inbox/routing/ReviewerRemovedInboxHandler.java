package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.review.ReviewerRemovedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewerRemovedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewerRemovedInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final ReviewerRemovedService reviewerRemovedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.REVIEWER_REMOVED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        ReviewerRemovedRequest request = deserializer.deserialize(context, supportType(), ReviewerRemovedRequest.class);
        reviewerRemovedService.removeReviewer(request);
    }
}
