package com.prism.statistics.application.analysis.insight.publisher;

import com.prism.statistics.application.analysis.insight.PullRequestMetricsEvent;
import com.prism.statistics.application.analysis.insight.PullRequestMetricsPublisher;
import com.prism.statistics.application.analysis.insight.PullRequestMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AsyncPullRequestMetricsPublisher implements PullRequestMetricsPublisher {

    private final PullRequestMetricsService metricsService;

    @Override
    @Async("asyncTaskExecutor")
    public void publish(PullRequestMetricsEvent event) {
        metricsService.deriveMetrics(event.pullRequestId());
    }
}
