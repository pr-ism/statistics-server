package com.prism.statistics.application.analysis.insight.publisher;

import com.prism.statistics.application.analysis.insight.ReviewActivityMetricsService;
import com.prism.statistics.application.analysis.insight.ReviewActivityPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AsyncReviewActivityPublisher implements ReviewActivityPublisher {

    private final ReviewActivityMetricsService reviewActivityMetricsService;

    @Override
    @Async("asyncTaskExecutor")
    public void publish(Long reviewId) {
        reviewActivityMetricsService.deriveMetrics(reviewId);
    }
}
