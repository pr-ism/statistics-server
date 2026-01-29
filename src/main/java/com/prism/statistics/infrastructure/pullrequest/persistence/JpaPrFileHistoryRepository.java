package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PrFileHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPrFileHistoryRepository extends ListCrudRepository<PrFileHistory, Long> {
}
