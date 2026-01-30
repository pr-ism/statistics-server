package com.prism.statistics.infrastructure.label.persistence;

import com.prism.statistics.domain.label.PrLabel;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPrLabelRepository extends ListCrudRepository<PrLabel, Long> {
}
