package com.prism.statistics.application.analysis.insight.publisher;

import com.prism.statistics.application.analysis.insight.PullRequestClosureMetricsPublisher;
import com.prism.statistics.application.analysis.insight.PullRequestClosureMetricsService;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AsyncPullRequestClosureMetricsPublisher implements PullRequestClosureMetricsPublisher {

    private final PullRequestClosureMetricsService closureMetricsService;

    @Override
    @Async("asyncTaskExecutor")
    public void publish(Long pullRequestId, PullRequestState newState, LocalDateTime closedAt) {
        closureMetricsService.deriveClosureMetrics(pullRequestId, newState, closedAt);
    }
}
