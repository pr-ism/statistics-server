package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestReadyForReviewService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReadyForReviewRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestReadyForReviewInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final PullRequestReadyForReviewService pullRequestReadyForReviewService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_READY_FOR_REVIEW;
    }

    @Override
    public void handle(CollectInboxContext context) {
        PullRequestReadyForReviewRequest request = deserializer.deserialize(context, supportType(), PullRequestReadyForReviewRequest.class);
        pullRequestReadyForReviewService.readyForReview(context.projectId(), request);
    }
}
