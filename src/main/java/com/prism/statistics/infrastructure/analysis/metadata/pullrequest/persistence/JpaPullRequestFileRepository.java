package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestFile;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestFileRepository extends ListCrudRepository<PullRequestFile, Long> {
}
