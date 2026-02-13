package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.lifecycle.PullRequestLifecycle;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface JpaPullRequestLifecycleRepository extends ListCrudRepository<PullRequestLifecycle, Long> {

    Optional<PullRequestLifecycle> findByPullRequestId(Long pullRequestId);

    boolean existsByPullRequestId(Long pullRequestId);
}
