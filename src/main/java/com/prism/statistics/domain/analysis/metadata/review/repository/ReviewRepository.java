package com.prism.statistics.domain.analysis.metadata.review.repository;

import com.prism.statistics.domain.analysis.metadata.review.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

    Review saveOrFind(Review review);

    Optional<Review> findByGithubReviewId(Long githubReviewId);

    Optional<Long> findIdByGithubReviewId(Long githubReviewId);

    List<Review> findAllByPullRequestId(Long pullRequestId);

    long backfillPullRequestId(Long githubPullRequestId, Long pullRequestId);
}
