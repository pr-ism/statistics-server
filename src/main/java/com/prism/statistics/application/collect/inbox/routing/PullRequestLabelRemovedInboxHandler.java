package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestLabelRemovedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelRemovedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestLabelRemovedInboxHandler implements CollectInboxEventHandler {

    private final CollectInboxPayloadDeserializer deserializer;
    private final PullRequestLabelRemovedService pullRequestLabelRemovedService;

    @Override
    public CollectInboxType supportType() {
        return CollectInboxType.PULL_REQUEST_LABEL_REMOVED;
    }

    @Override
    public void handle(CollectInboxContext context) {
        PullRequestLabelRemovedRequest request = deserializer.deserialize(context, supportType(), PullRequestLabelRemovedRequest.class);
        pullRequestLabelRemovedService.removePullRequestLabel(request);
    }
}
