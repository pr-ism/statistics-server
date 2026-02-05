package com.prism.statistics.infrastructure.analysis.metadata.review.persistence;

import com.prism.statistics.domain.analysis.metadata.review.history.RequestedReviewerHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaRequestedReviewerHistoryRepository extends ListCrudRepository<RequestedReviewerHistory, Long> {
}
