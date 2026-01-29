package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PrChangeHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPrChangeHistoryRepository extends ListCrudRepository<PrChangeHistory, Long> {
}
