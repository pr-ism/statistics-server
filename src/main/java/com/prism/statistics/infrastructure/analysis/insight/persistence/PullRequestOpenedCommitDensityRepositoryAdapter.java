package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedCommitDensity;
import com.prism.statistics.domain.analysis.insight.repository.PullRequestOpenedCommitDensityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PullRequestOpenedCommitDensityRepositoryAdapter implements PullRequestOpenedCommitDensityRepository {

    private final JpaPullRequestOpenedCommitDensityRepository jpaPullRequestOpenedCommitDensityRepository;

    @Override
    public PullRequestOpenedCommitDensity save(PullRequestOpenedCommitDensity density) {
        return jpaPullRequestOpenedCommitDensityRepository.save(density);
    }
}
