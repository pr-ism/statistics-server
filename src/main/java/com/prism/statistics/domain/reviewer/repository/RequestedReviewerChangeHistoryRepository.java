package com.prism.statistics.domain.reviewer.repository;

import com.prism.statistics.domain.reviewer.RequestedReviewerChangeHistory;

public interface RequestedReviewerChangeHistoryRepository {

    RequestedReviewerChangeHistory save(RequestedReviewerChangeHistory requestedReviewerChangeHistory);
}
