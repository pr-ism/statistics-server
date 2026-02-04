package com.prism.statistics.domain.analysis.insight.repository;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedCommitDensity;

public interface PullRequestOpenedCommitDensityRepository {

    PullRequestOpenedCommitDensity save(PullRequestOpenedCommitDensity density);
}
