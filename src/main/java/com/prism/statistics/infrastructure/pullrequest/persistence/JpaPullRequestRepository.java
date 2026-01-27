package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequest;
import org.springframework.data.repository.CrudRepository;

public interface JpaPullRequestRepository extends CrudRepository<PullRequest, Long> {
}
