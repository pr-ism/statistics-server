package com.prism.statistics.infrastructure.label.persistence;

import com.prism.statistics.domain.label.PrLabelHistory;
import com.prism.statistics.domain.label.repository.PrLabelHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PrLabelHistoryRepositoryAdapter implements PrLabelHistoryRepository {

    private final JpaPrLabelHistoryRepository jpaPrLabelHistoryRepository;

    @Override
    @Transactional
    public PrLabelHistory save(PrLabelHistory prLabelHistory) {
        return jpaPrLabelHistoryRepository.save(prLabelHistory);
    }
}
