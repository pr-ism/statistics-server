package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.activity.ReviewActivity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface JpaReviewActivityRepository extends ListCrudRepository<ReviewActivity, Long> {

    Optional<ReviewActivity> findByPullRequestId(Long pullRequestId);

    boolean existsByPullRequestId(Long pullRequestId);
}
