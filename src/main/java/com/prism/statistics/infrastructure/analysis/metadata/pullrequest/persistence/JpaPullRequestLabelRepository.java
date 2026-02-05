package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestLabel;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestLabelRepository extends ListCrudRepository<PullRequestLabel, Long> {
}
