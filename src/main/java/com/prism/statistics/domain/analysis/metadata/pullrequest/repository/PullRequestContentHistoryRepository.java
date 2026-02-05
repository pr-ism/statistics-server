package com.prism.statistics.domain.analysis.metadata.pullrequest.repository;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestContentHistory;

public interface PullRequestContentHistoryRepository {

    PullRequestContentHistory save(PullRequestContentHistory pullRequestContentHistory);
}
