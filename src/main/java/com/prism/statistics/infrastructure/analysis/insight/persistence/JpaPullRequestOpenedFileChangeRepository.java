package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedFileChange;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestOpenedFileChangeRepository extends ListCrudRepository<PullRequestOpenedFileChange, Long> {
}
