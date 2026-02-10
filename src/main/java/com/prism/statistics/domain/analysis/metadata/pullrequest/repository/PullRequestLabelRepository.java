package com.prism.statistics.domain.analysis.metadata.pullrequest.repository;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestLabel;

import java.util.Optional;

public interface PullRequestLabelRepository {

    PullRequestLabel saveOrFind(PullRequestLabel pullRequestLabel);

    Optional<PullRequestLabel> findByGithubPullRequestIdAndLabelName(Long githubPullRequestId, String labelName);

    boolean exists(Long pullRequestId, String labelName);

    long deleteLabel(Long pullRequestId, String labelName);

    long deleteLabelByGithubId(Long githubPullRequestId, String labelName);
}
