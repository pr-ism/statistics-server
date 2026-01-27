package com.prism.statistics.domain.pullrequest.repository;

import com.prism.statistics.domain.pullrequest.PrChangeHistory;

public interface PrChangeHistoryRepository {

    PrChangeHistory save(PrChangeHistory prChangeHistory);
}
