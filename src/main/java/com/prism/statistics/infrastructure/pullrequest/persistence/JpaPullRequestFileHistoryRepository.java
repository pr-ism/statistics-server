package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequestFileHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestFileHistoryRepository extends ListCrudRepository<PullRequestFileHistory, Long> {
}
