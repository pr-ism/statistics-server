package com.prism.statistics.domain.pullrequest.repository;

import com.prism.statistics.domain.pullrequest.PullRequestStateHistory;

public interface PullRequestStateHistoryRepository {

    PullRequestStateHistory save(PullRequestStateHistory pullRequestStateHistory);
}
