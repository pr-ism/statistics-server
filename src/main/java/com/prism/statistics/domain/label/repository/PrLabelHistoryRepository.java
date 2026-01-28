package com.prism.statistics.domain.label.repository;

import com.prism.statistics.domain.label.PrLabelHistory;

public interface PrLabelHistoryRepository {

    PrLabelHistory save(PrLabelHistory prLabelHistory);
}
