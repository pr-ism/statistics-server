package com.prism.statistics.application.collect.inbox.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestOpenedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestOpenedInboxHandler implements CollectInboxEventHandler {

    private final ObjectMapper objectMapper;
    private final PullRequestOpenedService pullRequestOpenedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_OPENED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        try {
            PullRequestOpenedRequest request = objectMapper.readValue(context.payloadJson(), PullRequestOpenedRequest.class);
            pullRequestOpenedService.createPullRequest(context.projectId(), request);
        } catch (Exception e) {
            throw new RuntimeException(supportType() + " 핸들러 처리 중 예외가 발생했습니다.", e);
        }
    }
}
