package com.prism.statistics.infrastructure.reviewer.persistence;

import com.prism.statistics.domain.reviewer.RequestedReviewer;
import org.springframework.data.repository.CrudRepository;

public interface JpaRequestedReviewerRepository extends CrudRepository<RequestedReviewer, Long> {
}
