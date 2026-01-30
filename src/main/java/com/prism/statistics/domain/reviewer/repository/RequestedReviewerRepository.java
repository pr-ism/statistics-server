package com.prism.statistics.domain.reviewer.repository;

import com.prism.statistics.domain.reviewer.RequestedReviewer;

public interface RequestedReviewerRepository {

    RequestedReviewer save(RequestedReviewer requestedReviewer);

    boolean exists(Long pullRequestId, Long githubUid);

    long delete(Long pullRequestId, Long githubUid);
}
