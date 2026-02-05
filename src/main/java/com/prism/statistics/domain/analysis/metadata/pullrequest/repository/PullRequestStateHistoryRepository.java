package com.prism.statistics.domain.analysis.metadata.pullrequest.repository;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestStateHistory;

public interface PullRequestStateHistoryRepository {

    PullRequestStateHistory save(PullRequestStateHistory pullRequestStateHistory);
}
