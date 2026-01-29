package com.prism.statistics.infrastructure.reviewer.persistence;

import com.prism.statistics.domain.reviewer.RequestedReviewerHistory;
import org.springframework.data.repository.CrudRepository;

public interface JpaRequestedReviewerHistoryRepository extends CrudRepository<RequestedReviewerHistory, Long> {
}
