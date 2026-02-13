package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck;
import com.prism.statistics.domain.analysis.insight.bottleneck.repository.PullRequestBottleneckRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PullRequestBottleneckRepositoryAdapter implements PullRequestBottleneckRepository {

    private final JpaPullRequestBottleneckRepository jpaPullRequestBottleneckRepository;

    @Override
    @Transactional
    public PullRequestBottleneck save(PullRequestBottleneck bottleneck) {
        return jpaPullRequestBottleneckRepository.save(bottleneck);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PullRequestBottleneck> findByPullRequestId(Long pullRequestId) {
        return jpaPullRequestBottleneckRepository.findByPullRequestId(pullRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPullRequestId(Long pullRequestId) {
        return jpaPullRequestBottleneckRepository.existsByPullRequestId(pullRequestId);
    }
}
