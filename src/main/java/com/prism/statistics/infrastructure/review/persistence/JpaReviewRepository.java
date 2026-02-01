package com.prism.statistics.infrastructure.review.persistence;

import com.prism.statistics.domain.review.Review;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaReviewRepository extends ListCrudRepository<Review, Long> {
}
