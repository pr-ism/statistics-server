package com.prism.statistics.application.analysis.insight;

public interface PullRequestMetricsPublisher {

    void publish(PullRequestMetricsEvent event);
}
