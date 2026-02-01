package com.prism.statistics.infrastructure.review.persistence;

import com.prism.statistics.domain.review.Review;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface JpaReviewRepository extends ListCrudRepository<Review, Long> {

    Optional<Review> findByGithubReviewId(Long githubReviewId);
}
