package com.prism.statistics.infrastructure.label.persistence;

import com.prism.statistics.domain.label.PullRequestLabelHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestLabelHistoryRepository extends ListCrudRepository<PullRequestLabelHistory, Long> {
}
