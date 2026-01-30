package com.prism.statistics.domain.label.repository;

import com.prism.statistics.domain.label.PullRequestLabelHistory;

public interface PullRequestLabelHistoryRepository {

    PullRequestLabelHistory save(PullRequestLabelHistory pullRequestLabelHistory);
}
