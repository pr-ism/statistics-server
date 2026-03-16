package com.prism.statistics.application.collect.inbox.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestConvertedToDraftService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestConvertedToDraftRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestConvertedToDraftInboxHandler implements CollectInboxEventHandler {

    private final ObjectMapper objectMapper;
    private final PullRequestConvertedToDraftService pullRequestConvertedToDraftService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_CONVERTED_TO_DRAFT;
    }

    @Override
    public void handle(CollectInboxContext context) {
        try {
            PullRequestConvertedToDraftRequest request = objectMapper.readValue(context.payloadJson(), PullRequestConvertedToDraftRequest.class);
            pullRequestConvertedToDraftService.convertToDraft(context.projectId(), request);
        } catch (Exception e) {
            throw new RuntimeException(supportType() + " 핸들러 처리 중 예외가 발생했습니다.", e);
        }
    }
}
