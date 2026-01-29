package com.prism.statistics.infrastructure.reviewer.persistence;

import com.prism.statistics.domain.reviewer.RequestedReviewer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRequestedReviewerRepository extends JpaRepository<RequestedReviewer, Long> {
}
