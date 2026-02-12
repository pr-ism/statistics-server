package com.prism.statistics.domain.analysis.metadata.pullrequest.repository;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestLabelHistory;

public interface PullRequestLabelHistoryRepository {

    PullRequestLabelHistory save(PullRequestLabelHistory pullRequestLabelHistory);

    long backfillPullRequestId(Long githubPullRequestId, Long pullRequestId);
}
