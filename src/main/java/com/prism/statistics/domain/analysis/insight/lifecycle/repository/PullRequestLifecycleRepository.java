package com.prism.statistics.domain.analysis.insight.lifecycle.repository;

import com.prism.statistics.domain.analysis.insight.lifecycle.PullRequestLifecycle;

import java.util.Optional;

public interface PullRequestLifecycleRepository {

    PullRequestLifecycle save(PullRequestLifecycle lifecycle);

    Optional<PullRequestLifecycle> findByPullRequestId(Long pullRequestId);

    boolean existsByPullRequestId(Long pullRequestId);
}
