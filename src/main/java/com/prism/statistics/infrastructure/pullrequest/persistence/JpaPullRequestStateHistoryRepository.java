package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequestStateHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestStateHistoryRepository extends ListCrudRepository<PullRequestStateHistory, Long> {
}
