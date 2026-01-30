package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequest;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestRepository extends ListCrudRepository<PullRequest, Long> {
}
