package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequestFile;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestFileRepository extends ListCrudRepository<PullRequestFile, Long> {
}
