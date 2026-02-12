package com.prism.statistics.domain.analysis.metadata.review.repository;

import com.prism.statistics.domain.analysis.metadata.review.RequestedReviewer;

import java.util.Optional;

public interface RequestedReviewerRepository {

    RequestedReviewer saveOrFind(RequestedReviewer requestedReviewer);

    Optional<RequestedReviewer> findByGithubPullRequestIdAndUserId(Long githubPullRequestId, Long userId);

    boolean exists(Long pullRequestId, Long githubUid);

    long delete(Long pullRequestId, Long githubUid);

    long deleteByGithubId(Long githubPullRequestId, Long userId);

    long backfillPullRequestId(Long githubPullRequestId, Long pullRequestId);
}
