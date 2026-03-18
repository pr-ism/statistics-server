package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestConvertedToDraftService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestConvertedToDraftRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestConvertedToDraftInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final PullRequestConvertedToDraftService pullRequestConvertedToDraftService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_CONVERTED_TO_DRAFT;
    }

    @Override
    public void handle(CollectInboxContext context) {
        PullRequestConvertedToDraftRequest request = deserializer.deserialize(context, supportType(), PullRequestConvertedToDraftRequest.class);
        pullRequestConvertedToDraftService.convertToDraft(context.projectId(), request);
    }
}
