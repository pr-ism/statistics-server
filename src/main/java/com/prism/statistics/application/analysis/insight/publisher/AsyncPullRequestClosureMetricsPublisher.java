package com.prism.statistics.application.analysis.insight.publisher;

import com.prism.statistics.application.analysis.insight.PullRequestClosureMetricsEvent;
import com.prism.statistics.application.analysis.insight.PullRequestClosureMetricsPublisher;
import com.prism.statistics.application.analysis.insight.PullRequestClosureMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AsyncPullRequestClosureMetricsPublisher implements PullRequestClosureMetricsPublisher {

    private final PullRequestClosureMetricsService closureMetricsService;

    @Override
    @Async("asyncTaskExecutor")
    public void publish(PullRequestClosureMetricsEvent event) {
        closureMetricsService.deriveClosureMetrics(
                event.pullRequestId(),
                event.newState(),
                event.closedAt()
        );
    }
}
