package com.prism.statistics.application.analysis.insight;

import com.prism.statistics.application.webhook.event.PullRequestOpenCreatedEvent;

public interface PullRequestMetricsPublisher {

    void publish(PullRequestOpenCreatedEvent event);
}
