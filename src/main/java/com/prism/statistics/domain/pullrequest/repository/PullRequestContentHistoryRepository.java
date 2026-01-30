package com.prism.statistics.domain.pullrequest.repository;

import com.prism.statistics.domain.pullrequest.PullRequestContentHistory;

public interface PullRequestContentHistoryRepository {

    PullRequestContentHistory save(PullRequestContentHistory pullRequestContentHistory);
}
