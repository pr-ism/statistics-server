package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PrFileHistory;
import com.prism.statistics.domain.pullrequest.repository.PrFileHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PrFileHistoryRepositoryAdapter implements PrFileHistoryRepository {

    private final JpaPrFileHistoryRepository jpaPrFileHistoryRepository;

    @Override
    public PrFileHistory save(PrFileHistory prFileHistory) {
        return jpaPrFileHistoryRepository.save(prFileHistory);
    }

    @Override
    public List<PrFileHistory> saveAll(List<PrFileHistory> prFileHistories) {
        return jpaPrFileHistoryRepository.saveAll(prFileHistories);
    }
}
