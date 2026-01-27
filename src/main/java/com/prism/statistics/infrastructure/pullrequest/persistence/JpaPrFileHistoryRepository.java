package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PrFileHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPrFileHistoryRepository extends JpaRepository<PrFileHistory, Long> {
}
