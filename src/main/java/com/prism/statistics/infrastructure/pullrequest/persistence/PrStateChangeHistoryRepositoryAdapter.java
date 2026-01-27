package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PrStateChangeHistory;
import com.prism.statistics.domain.pullrequest.repository.PrStateChangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PrStateChangeHistoryRepositoryAdapter implements PrStateChangeHistoryRepository {

    private final JpaPrStateChangeHistoryRepository jpaPrStateChangeHistoryRepository;

    @Override
    public PrStateChangeHistory save(PrStateChangeHistory prStateChangeHistory) {
        return jpaPrStateChangeHistoryRepository.save(prStateChangeHistory);
    }
}
