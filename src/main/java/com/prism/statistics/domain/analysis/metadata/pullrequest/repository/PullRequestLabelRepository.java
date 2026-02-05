package com.prism.statistics.domain.analysis.metadata.pullrequest.repository;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestLabel;

public interface PullRequestLabelRepository {

    PullRequestLabel save(PullRequestLabel pullRequestLabel);

    boolean exists(Long pullRequestId, String labelName);

    long deleteLabel(Long pullRequestId, String labelName);
}
