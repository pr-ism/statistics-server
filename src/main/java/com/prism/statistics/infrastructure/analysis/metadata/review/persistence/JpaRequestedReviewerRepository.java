package com.prism.statistics.infrastructure.analysis.metadata.review.persistence;

import com.prism.statistics.domain.analysis.metadata.review.RequestedReviewer;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaRequestedReviewerRepository extends ListCrudRepository<RequestedReviewer, Long> {
}
