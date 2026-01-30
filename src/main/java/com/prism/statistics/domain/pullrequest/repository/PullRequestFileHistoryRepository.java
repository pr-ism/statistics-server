package com.prism.statistics.domain.pullrequest.repository;

import com.prism.statistics.domain.pullrequest.PullRequestFileHistory;

import java.util.List;

public interface PullRequestFileHistoryRepository {

    PullRequestFileHistory save(PullRequestFileHistory pullRequestFileHistory);

    List<PullRequestFileHistory> saveAll(List<PullRequestFileHistory> pullRequestFileHistories);
}
