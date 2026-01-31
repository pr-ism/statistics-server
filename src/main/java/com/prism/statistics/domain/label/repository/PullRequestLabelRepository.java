package com.prism.statistics.domain.label.repository;

import com.prism.statistics.domain.label.PullRequestLabel;

public interface PullRequestLabelRepository {

    PullRequestLabel save(PullRequestLabel pullRequestLabel);

    boolean exists(Long pullRequestId, String labelName);

    long deleteLabel(Long pullRequestId, String labelName);
}
