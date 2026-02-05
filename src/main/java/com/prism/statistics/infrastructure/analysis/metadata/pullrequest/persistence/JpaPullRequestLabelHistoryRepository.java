package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestLabelHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestLabelHistoryRepository extends ListCrudRepository<PullRequestLabelHistory, Long> {
}
