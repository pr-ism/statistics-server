package com.prism.statistics.domain.reviewer.repository;

import com.prism.statistics.domain.reviewer.RequestedReviewerHistory;

public interface RequestedReviewerHistoryRepository {

    RequestedReviewerHistory save(RequestedReviewerHistory requestedReviewerHistory);
}
