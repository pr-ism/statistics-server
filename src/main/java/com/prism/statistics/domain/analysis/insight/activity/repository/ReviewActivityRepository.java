package com.prism.statistics.domain.analysis.insight.activity.repository;

import com.prism.statistics.domain.analysis.insight.activity.ReviewActivity;

import java.util.Optional;

public interface ReviewActivityRepository {

    ReviewActivity save(ReviewActivity activity);

    Optional<ReviewActivity> findByPullRequestId(Long pullRequestId);

    boolean existsByPullRequestId(Long pullRequestId);
}
