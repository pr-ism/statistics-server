package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PrStateChangeHistory;
import org.springframework.data.repository.CrudRepository;

public interface JpaPrStateChangeHistoryRepository extends CrudRepository<PrStateChangeHistory, Long> {
}
