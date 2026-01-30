package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequestStateHistory;
import com.prism.statistics.domain.pullrequest.repository.PullRequestStateHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PullRequestStateHistoryRepositoryAdapter implements PullRequestStateHistoryRepository {

    private final JpaPullRequestStateHistoryRepository jpaPullRequestStateHistoryRepository;

    @Override
    public PullRequestStateHistory save(PullRequestStateHistory prStateChangeHistory) {
        return jpaPullRequestStateHistoryRepository.save(prStateChangeHistory);
    }
}
