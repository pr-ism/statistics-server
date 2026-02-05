package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestContentHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestContentHistoryRepository extends ListCrudRepository<PullRequestContentHistory, Long> {
}
