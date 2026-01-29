package com.prism.statistics.infrastructure.label.persistence;

import com.prism.statistics.domain.label.PrLabelHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPrLabelHistoryRepository extends ListCrudRepository<PrLabelHistory, Long> {
}
