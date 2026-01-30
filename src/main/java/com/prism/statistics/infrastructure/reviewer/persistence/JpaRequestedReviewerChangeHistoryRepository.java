package com.prism.statistics.infrastructure.reviewer.persistence;

import com.prism.statistics.domain.reviewer.RequestedReviewerChangeHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaRequestedReviewerChangeHistoryRepository extends ListCrudRepository<RequestedReviewerChangeHistory, Long> {
}
