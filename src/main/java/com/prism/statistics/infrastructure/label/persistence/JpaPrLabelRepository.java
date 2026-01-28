package com.prism.statistics.infrastructure.label.persistence;

import com.prism.statistics.domain.label.PrLabel;
import org.springframework.data.repository.CrudRepository;

public interface JpaPrLabelRepository extends CrudRepository<PrLabel, Long> {
}
