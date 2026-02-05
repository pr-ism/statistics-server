package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestStateHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestStateHistoryRepository extends ListCrudRepository<PullRequestStateHistory, Long> {
}
