package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedSizeMetrics;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestOpenedSizeMetricsRepository extends ListCrudRepository<PullRequestOpenedSizeMetrics, Long> {
}
