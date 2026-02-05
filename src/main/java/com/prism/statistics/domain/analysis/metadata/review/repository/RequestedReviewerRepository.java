package com.prism.statistics.domain.analysis.metadata.review.repository;

import com.prism.statistics.domain.analysis.metadata.review.RequestedReviewer;

public interface RequestedReviewerRepository {

    RequestedReviewer save(RequestedReviewer requestedReviewer);

    boolean exists(Long pullRequestId, Long githubUid);

    long delete(Long pullRequestId, Long githubUid);
}
