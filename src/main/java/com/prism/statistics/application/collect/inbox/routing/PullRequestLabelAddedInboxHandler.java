package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestLabelAddedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelAddedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestLabelAddedInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final PullRequestLabelAddedService pullRequestLabelAddedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_LABEL_ADDED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        PullRequestLabelAddedRequest request = deserializer.deserialize(context, supportType(), PullRequestLabelAddedRequest.class);
        pullRequestLabelAddedService.addPullRequestLabel(request);
    }
}
