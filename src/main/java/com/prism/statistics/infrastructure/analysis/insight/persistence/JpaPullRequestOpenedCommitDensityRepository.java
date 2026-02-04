package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedCommitDensity;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestOpenedCommitDensityRepository extends ListCrudRepository<PullRequestOpenedCommitDensity, Long> {
}
