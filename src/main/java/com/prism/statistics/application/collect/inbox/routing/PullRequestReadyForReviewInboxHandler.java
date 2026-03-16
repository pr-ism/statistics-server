package com.prism.statistics.application.collect.inbox.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestReadyForReviewService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReadyForReviewRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestReadyForReviewInboxHandler implements CollectInboxEventHandler {

    private final ObjectMapper objectMapper;
    private final PullRequestReadyForReviewService pullRequestReadyForReviewService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_READY_FOR_REVIEW;
    }

    @Override
    public void handle(CollectInboxContext context) {
        try {
            PullRequestReadyForReviewRequest request = objectMapper.readValue(context.payloadJson(), PullRequestReadyForReviewRequest.class);
            pullRequestReadyForReviewService.readyForReview(context.projectId(), request);
        } catch (Exception e) {
            throw new RuntimeException(supportType() + " 핸들러 처리 중 예외가 발생했습니다.", e);
        }
    }
}
