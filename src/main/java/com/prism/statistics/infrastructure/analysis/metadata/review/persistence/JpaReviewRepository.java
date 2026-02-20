package com.prism.statistics.infrastructure.analysis.metadata.review.persistence;

import com.prism.statistics.domain.analysis.metadata.review.Review;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface JpaReviewRepository extends ListCrudRepository<Review, Long> {

    Optional<Review> findByGithubReviewId(Long githubReviewId);

    List<Review> findAllByPullRequestId(Long pullRequestId);
}
