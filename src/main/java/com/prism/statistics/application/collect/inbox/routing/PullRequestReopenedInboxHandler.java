package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestReopenedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReopenedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestReopenedInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final PullRequestReopenedService pullRequestReopenedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_REOPENED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        PullRequestReopenedRequest request = deserializer.deserialize(context, supportType(), PullRequestReopenedRequest.class);
        pullRequestReopenedService.reopenPullRequest(context.projectId(), request);
    }
}
