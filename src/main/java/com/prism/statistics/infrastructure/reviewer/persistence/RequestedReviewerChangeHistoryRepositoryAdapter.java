package com.prism.statistics.infrastructure.reviewer.persistence;

import com.prism.statistics.domain.reviewer.RequestedReviewerChangeHistory;
import com.prism.statistics.domain.reviewer.repository.RequestedReviewerChangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class RequestedReviewerChangeHistoryRepositoryAdapter implements RequestedReviewerChangeHistoryRepository {

    private final JpaRequestedReviewerChangeHistoryRepository jpaRequestedReviewerChangeHistoryRepository;

    @Override
    @Transactional
    public RequestedReviewerChangeHistory save(RequestedReviewerChangeHistory requestedReviewerChangeHistory) {
        return jpaRequestedReviewerChangeHistoryRepository.save(requestedReviewerChangeHistory);
    }
}
