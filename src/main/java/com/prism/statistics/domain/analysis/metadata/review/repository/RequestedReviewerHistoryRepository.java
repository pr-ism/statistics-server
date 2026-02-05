package com.prism.statistics.domain.analysis.metadata.review.repository;

import com.prism.statistics.domain.analysis.metadata.review.history.RequestedReviewerHistory;

public interface RequestedReviewerHistoryRepository {

    RequestedReviewerHistory save(RequestedReviewerHistory requestedReviewerHistory);
}
