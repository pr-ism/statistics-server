package com.prism.statistics.application.analysis.insight;

import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;

public interface PullRequestMetricsPublisher {

    void publish(PullRequestOpenCreatedEvent event);
}
