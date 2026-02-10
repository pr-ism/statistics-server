package com.prism.statistics.domain.analysis.insight.repository;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedSizeMetrics;

public interface PullRequestOpenedSizeMetricsRepository {

    PullRequestOpenedSizeMetrics save(PullRequestOpenedSizeMetrics sizeMetrics);
}
