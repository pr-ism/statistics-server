package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PrChangeHistory;
import com.prism.statistics.domain.pullrequest.repository.PrChangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PrChangeHistoryRepositoryAdapter implements PrChangeHistoryRepository {

    private final JpaPrChangeHistoryRepository jpaPrChangeHistoryRepository;

    @Override
    public PrChangeHistory save(PrChangeHistory prChangeHistory) {
        return jpaPrChangeHistoryRepository.save(prChangeHistory);
    }
}
