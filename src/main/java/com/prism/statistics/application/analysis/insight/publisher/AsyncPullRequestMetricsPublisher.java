package com.prism.statistics.application.analysis.insight.publisher;

import com.prism.statistics.application.analysis.insight.PullRequestMetricsPublisher;
import com.prism.statistics.application.analysis.insight.PullRequestMetricsService;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AsyncPullRequestMetricsPublisher implements PullRequestMetricsPublisher {

    private final PullRequestMetricsService metricsService;

    @Override
    @Async("asyncTaskExecutor")
    public void publish(PullRequestOpenCreatedEvent event) {
        metricsService.deriveMetrics(event);
    }
}
