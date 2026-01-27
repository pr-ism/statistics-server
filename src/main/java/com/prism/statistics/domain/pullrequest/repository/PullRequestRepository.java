package com.prism.statistics.domain.pullrequest.repository;

import com.prism.statistics.domain.pullrequest.PullRequest;

public interface PullRequestRepository {

    PullRequest save(PullRequest pullRequest);
}
