package com.prism.statistics.domain.analysis.metadata.pullrequest.repository;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestFileHistory;

import java.util.List;

public interface PullRequestFileHistoryRepository {

    PullRequestFileHistory save(PullRequestFileHistory pullRequestFileHistory);

    List<PullRequestFileHistory> saveAll(List<PullRequestFileHistory> pullRequestFileHistories);

    long backfillPullRequestId(Long githubPullRequestId, Long pullRequestId);
}
