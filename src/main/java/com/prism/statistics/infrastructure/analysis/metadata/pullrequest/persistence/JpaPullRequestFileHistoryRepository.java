package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestFileHistory;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestFileHistoryRepository extends ListCrudRepository<PullRequestFileHistory, Long> {
}
