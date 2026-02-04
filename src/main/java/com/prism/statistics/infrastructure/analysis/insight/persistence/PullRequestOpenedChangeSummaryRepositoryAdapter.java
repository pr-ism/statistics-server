package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedChangeSummary;
import com.prism.statistics.domain.analysis.insight.repository.PullRequestOpenedChangeSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PullRequestOpenedChangeSummaryRepositoryAdapter implements PullRequestOpenedChangeSummaryRepository {

    private final JpaPullRequestOpenedChangeSummaryRepository jpaPullRequestOpenedChangeSummaryRepository;

    @Override
    public PullRequestOpenedChangeSummary save(PullRequestOpenedChangeSummary summary) {
        return jpaPullRequestOpenedChangeSummaryRepository.save(summary);
    }
}
