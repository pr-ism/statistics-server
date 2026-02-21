package com.prism.statistics.domain.analysis.metadata.pullrequest.repository;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;

import java.util.List;
import java.util.Optional;

public interface PullRequestRepository {

    PullRequest save(PullRequest pullRequest);

    Optional<PullRequest> findById(Long id);

    Optional<PullRequest> findPullRequest(Long projectId, int pullRequestNumber);

    List<PullRequest> findAllByProjectId(Long projectId);

    Optional<Long> findIdByGithubId(Long githubPullRequestId);
}
