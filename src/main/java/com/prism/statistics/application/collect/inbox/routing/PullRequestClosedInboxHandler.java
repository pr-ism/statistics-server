package com.prism.statistics.application.collect.inbox.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestClosedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestClosedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestClosedInboxHandler implements CollectInboxEventHandler {

    private final ObjectMapper objectMapper;
    private final PullRequestClosedService pullRequestClosedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_CLOSED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        try {
            PullRequestClosedRequest request = objectMapper.readValue(context.payloadJson(), PullRequestClosedRequest.class);
            pullRequestClosedService.closePullRequest(context.projectId(), request);
        } catch (Exception e) {
            throw new RuntimeException(supportType() + " 핸들러 처리 중 예외가 발생했습니다.", e);
        }
    }
}
