package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.review.ReviewerAddedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewerAddedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewerAddedInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final ReviewerAddedService reviewerAddedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.REVIEWER_ADDED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        ReviewerAddedRequest request = deserializer.deserialize(context, supportType(), ReviewerAddedRequest.class);
        reviewerAddedService.addReviewer(request);
    }
}
