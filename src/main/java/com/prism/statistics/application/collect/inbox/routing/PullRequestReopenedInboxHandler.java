package com.prism.statistics.application.collect.inbox.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestReopenedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReopenedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestReopenedInboxHandler implements CollectInboxEventHandler {

    private final ObjectMapper objectMapper;
    private final PullRequestReopenedService pullRequestReopenedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_REOPENED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        try {
            PullRequestReopenedRequest request = objectMapper.readValue(context.payloadJson(), PullRequestReopenedRequest.class);
            pullRequestReopenedService.reopenPullRequest(context.projectId(), request);
        } catch (Exception e) {
            throw new RuntimeException(supportType() + " 핸들러 처리 중 예외가 발생했습니다.", e);
        }
    }
}
