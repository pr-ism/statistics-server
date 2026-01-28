package com.prism.statistics.infrastructure.label.persistence;

import com.prism.statistics.domain.label.PrLabelHistory;
import org.springframework.data.repository.CrudRepository;

public interface JpaPrLabelHistoryRepository extends CrudRepository<PrLabelHistory, Long> {
}
