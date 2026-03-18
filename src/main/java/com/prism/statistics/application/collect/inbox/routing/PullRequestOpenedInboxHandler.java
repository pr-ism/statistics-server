package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestOpenedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestOpenedInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final PullRequestOpenedService pullRequestOpenedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_OPENED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        PullRequestOpenedRequest request = deserializer.deserialize(context, supportType(), PullRequestOpenedRequest.class);
        pullRequestOpenedService.createPullRequest(context.projectId(), request);
    }
}
