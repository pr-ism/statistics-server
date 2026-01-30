package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequestContentHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestContentHistoryRepository extends ListCrudRepository<PullRequestContentHistory, Long> {
}
