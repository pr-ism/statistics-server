package com.prism.statistics.domain.analysis.insight.lifecycle.repository;

import com.prism.statistics.domain.analysis.insight.lifecycle.PullRequestLifecycle;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PullRequestLifecycleRepository extends JpaRepository<PullRequestLifecycle, Long> {

    Optional<PullRequestLifecycle> findByPullRequestId(Long pullRequestId);

    boolean existsByPullRequestId(Long pullRequestId);
}
