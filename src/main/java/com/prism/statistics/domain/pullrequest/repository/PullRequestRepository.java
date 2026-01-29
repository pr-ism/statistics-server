package com.prism.statistics.domain.pullrequest.repository;

import com.prism.statistics.domain.pullrequest.PullRequest;

import java.util.Optional;

public interface PullRequestRepository {

    PullRequest save(PullRequest pullRequest);

    Optional<PullRequest> findPullRequest(Long projectId, int prNumber);

    Optional<PullRequest> findWithLock(Long projectId, int prNumber);
}
