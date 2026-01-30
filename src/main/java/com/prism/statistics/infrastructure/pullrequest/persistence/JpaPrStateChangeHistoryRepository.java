package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PrStateChangeHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPrStateChangeHistoryRepository extends ListCrudRepository<PrStateChangeHistory, Long> {
}
