package com.prism.statistics.domain.pullrequest.repository;

import com.prism.statistics.domain.pullrequest.PrFileHistory;

import java.util.List;

public interface PrFileHistoryRepository {

    PrFileHistory save(PrFileHistory prFileHistory);

    List<PrFileHistory> saveAll(List<PrFileHistory> prFileHistories);
}
