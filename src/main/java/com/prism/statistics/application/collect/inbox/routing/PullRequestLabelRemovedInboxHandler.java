package com.prism.statistics.application.collect.inbox.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestLabelRemovedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelRemovedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestLabelRemovedInboxHandler implements CollectInboxEventHandler {

    private final ObjectMapper objectMapper;
    private final PullRequestLabelRemovedService pullRequestLabelRemovedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_LABEL_REMOVED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        try {
            PullRequestLabelRemovedRequest request = objectMapper.readValue(context.payloadJson(), PullRequestLabelRemovedRequest.class);
            pullRequestLabelRemovedService.removePullRequestLabel(request);
        } catch (Exception e) {
            throw new RuntimeException(supportType() + " 핸들러 처리 중 예외가 발생했습니다.", e);
        }
    }
}
