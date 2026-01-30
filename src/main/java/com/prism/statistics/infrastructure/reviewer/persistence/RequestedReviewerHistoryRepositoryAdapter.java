package com.prism.statistics.infrastructure.reviewer.persistence;

import com.prism.statistics.domain.reviewer.RequestedReviewerHistory;
import com.prism.statistics.domain.reviewer.repository.RequestedReviewerHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class RequestedReviewerHistoryRepositoryAdapter implements RequestedReviewerHistoryRepository {

    private final JpaRequestedReviewerHistoryRepository jpaRequestedReviewerHistoryRepository;

    @Override
    @Transactional
    public RequestedReviewerHistory save(RequestedReviewerHistory requestedReviewerHistory) {
        return jpaRequestedReviewerHistoryRepository.save(requestedReviewerHistory);
    }
}
