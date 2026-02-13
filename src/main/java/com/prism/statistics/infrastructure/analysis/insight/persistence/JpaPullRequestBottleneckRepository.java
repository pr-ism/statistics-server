package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface JpaPullRequestBottleneckRepository extends ListCrudRepository<PullRequestBottleneck, Long> {

    Optional<PullRequestBottleneck> findByPullRequestId(Long pullRequestId);

    boolean existsByPullRequestId(Long pullRequestId);
}
