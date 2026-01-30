package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PrFile;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPrFileRepository extends ListCrudRepository<PrFile, Long> {
}
