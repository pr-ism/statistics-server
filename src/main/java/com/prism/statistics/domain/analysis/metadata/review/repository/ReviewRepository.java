package com.prism.statistics.domain.analysis.metadata.review.repository;

import com.prism.statistics.domain.analysis.metadata.review.Review;

import java.util.Optional;

public interface ReviewRepository {

    Review saveOrFind(Review review);

    Optional<Review> findByGithubReviewId(Long githubReviewId);
}
