package com.prism.statistics.infrastructure.reviewer.persistence;

import com.prism.statistics.domain.reviewer.RequestedReviewerHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaRequestedReviewerHistoryRepository extends ListCrudRepository<RequestedReviewerHistory, Long> {
}
