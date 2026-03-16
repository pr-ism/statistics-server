package com.prism.statistics.application.collect.inbox.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestLabelAddedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelAddedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestLabelAddedInboxHandler implements CollectInboxEventHandler {

    private final ObjectMapper objectMapper;
    private final PullRequestLabelAddedService pullRequestLabelAddedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_LABEL_ADDED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        try {
            PullRequestLabelAddedRequest request = objectMapper.readValue(context.payloadJson(), PullRequestLabelAddedRequest.class);
            pullRequestLabelAddedService.addPullRequestLabel(request);
        } catch (Exception e) {
            throw new RuntimeException(supportType() + " 핸들러 처리 중 예외가 발생했습니다.", e);
        }
    }
}
