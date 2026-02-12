package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.lifecycle.PullRequestLifecycle;
import com.prism.statistics.domain.analysis.insight.lifecycle.repository.PullRequestLifecycleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PullRequestLifecycleRepositoryAdapter implements PullRequestLifecycleRepository {

    private final JpaPullRequestLifecycleRepository jpaPullRequestLifecycleRepository;

    @Override
    public PullRequestLifecycle save(PullRequestLifecycle lifecycle) {
        return jpaPullRequestLifecycleRepository.save(lifecycle);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PullRequestLifecycle> findByPullRequestId(Long pullRequestId) {
        return jpaPullRequestLifecycleRepository.findByPullRequestId(pullRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPullRequestId(Long pullRequestId) {
        return jpaPullRequestLifecycleRepository.existsByPullRequestId(pullRequestId);
    }
}
