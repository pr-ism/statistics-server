package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.review.ReviewSubmittedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewSubmittedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewSubmittedInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final ReviewSubmittedService reviewSubmittedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.REVIEW_SUBMITTED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        ReviewSubmittedRequest request = deserializer.deserialize(context, supportType(), ReviewSubmittedRequest.class);
        reviewSubmittedService.submitReview(request);
    }
}
