package com.prism.statistics.domain.analysis.insight.review.repository;

import com.prism.statistics.domain.analysis.insight.review.ReviewResponseTime;

import java.util.Optional;

public interface ReviewResponseTimeRepository {

    ReviewResponseTime save(ReviewResponseTime responseTime);

    Optional<ReviewResponseTime> findByPullRequestId(Long pullRequestId);

    boolean existsByPullRequestId(Long pullRequestId);
}
