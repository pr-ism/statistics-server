package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedChangeSummary;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaPullRequestOpenedChangeSummaryRepository extends ListCrudRepository<PullRequestOpenedChangeSummary, Long> {
}
