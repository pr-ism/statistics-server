package com.prism.statistics.domain.pullrequest.repository;

import com.prism.statistics.domain.pullrequest.PrStateChangeHistory;

public interface PrStateChangeHistoryRepository {

    PrStateChangeHistory save(PrStateChangeHistory prStateChangeHistory);
}
