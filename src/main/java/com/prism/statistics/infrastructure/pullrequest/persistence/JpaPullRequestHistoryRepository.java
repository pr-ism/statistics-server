package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequestHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestHistoryRepository extends ListCrudRepository<PullRequestHistory, Long> {
}
