package com.prism.statistics.infrastructure.label.persistence;

import com.prism.statistics.domain.label.PullRequestLabel;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestLabelRepository extends ListCrudRepository<PullRequestLabel, Long> {
}
