package com.prism.statistics.application.analysis.insight;

public interface PullRequestClosureMetricsPublisher {

    void publish(PullRequestClosureMetricsEvent event);
}
