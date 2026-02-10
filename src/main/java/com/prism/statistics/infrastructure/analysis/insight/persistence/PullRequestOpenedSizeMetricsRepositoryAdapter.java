package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedSizeMetrics;
import com.prism.statistics.domain.analysis.insight.repository.PullRequestOpenedSizeMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PullRequestOpenedSizeMetricsRepositoryAdapter implements PullRequestOpenedSizeMetricsRepository {

    private final JpaPullRequestOpenedSizeMetricsRepository jpaPullRequestOpenedSizeMetricsRepository;

    @Override
    public PullRequestOpenedSizeMetrics save(PullRequestOpenedSizeMetrics sizeMetrics) {
        return jpaPullRequestOpenedSizeMetricsRepository.save(sizeMetrics);
    }
}
