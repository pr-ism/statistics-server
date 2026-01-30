package com.prism.statistics.infrastructure.reviewer.persistence;

import com.prism.statistics.domain.reviewer.RequestedReviewer;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaRequestedReviewerRepository extends ListCrudRepository<RequestedReviewer, Long> {
}
