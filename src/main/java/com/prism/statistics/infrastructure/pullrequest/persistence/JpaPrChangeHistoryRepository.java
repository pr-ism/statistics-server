package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PrChangeHistory;
import org.springframework.data.repository.CrudRepository;

public interface JpaPrChangeHistoryRepository extends CrudRepository<PrChangeHistory, Long> {
}
