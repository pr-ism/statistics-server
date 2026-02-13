package com.prism.statistics.domain.analysis.insight.bottleneck.repository;

import com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck;

import java.util.Optional;

public interface PullRequestBottleneckRepository {

    PullRequestBottleneck save(PullRequestBottleneck bottleneck);

    Optional<PullRequestBottleneck> findByPullRequestId(Long pullRequestId);

    boolean existsByPullRequestId(Long pullRequestId);
}
