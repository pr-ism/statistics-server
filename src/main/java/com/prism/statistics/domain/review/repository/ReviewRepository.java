package com.prism.statistics.domain.review.repository;

import com.prism.statistics.domain.review.Review;

import java.util.Optional;

public interface ReviewRepository {

    Review saveOrFind(Review review);

    Optional<Review> findByGithubReviewId(Long githubReviewId);
}
