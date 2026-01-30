package com.prism.statistics.domain.pullrequest.repository;

import com.prism.statistics.domain.pullrequest.PullRequestHistory;

public interface PullRequestHistoryRepository {

    PullRequestHistory save(PullRequestHistory pullRequestHistory);
}
