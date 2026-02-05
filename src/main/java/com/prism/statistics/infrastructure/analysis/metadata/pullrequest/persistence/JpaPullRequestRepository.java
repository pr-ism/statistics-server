package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestRepository extends ListCrudRepository<PullRequest, Long> {
}
