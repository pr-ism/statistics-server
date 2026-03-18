package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestClosedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestClosedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestClosedInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final PullRequestClosedService pullRequestClosedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_CLOSED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        PullRequestClosedRequest request = deserializer.deserialize(context, supportType(), PullRequestClosedRequest.class);
        pullRequestClosedService.closePullRequest(context.projectId(), request);
    }
}
