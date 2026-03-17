package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestSynchronizedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestSynchronizedInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final PullRequestSynchronizedService pullRequestSynchronizedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_SYNCHRONIZED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        PullRequestSynchronizedRequest request = deserializer.deserialize(context, supportType(), PullRequestSynchronizedRequest.class);
        pullRequestSynchronizedService.synchronizePullRequest(request);
    }
}
