package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.review.ReviewResponseTime;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface JpaReviewResponseTimeRepository extends ListCrudRepository<ReviewResponseTime, Long> {

    Optional<ReviewResponseTime> findByPullRequestId(Long pullRequestId);

    boolean existsByPullRequestId(Long pullRequestId);
}
