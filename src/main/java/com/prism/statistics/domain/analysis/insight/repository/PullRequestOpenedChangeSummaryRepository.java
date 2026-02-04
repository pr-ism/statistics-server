package com.prism.statistics.domain.analysis.insight.repository;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedChangeSummary;

public interface PullRequestOpenedChangeSummaryRepository {

    PullRequestOpenedChangeSummary save(PullRequestOpenedChangeSummary summary);
}
