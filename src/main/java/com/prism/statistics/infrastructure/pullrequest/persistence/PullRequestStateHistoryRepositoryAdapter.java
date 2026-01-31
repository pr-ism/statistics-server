package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequestStateHistory;
import com.prism.statistics.domain.pullrequest.repository.PullRequestStateHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PullRequestStateHistoryRepositoryAdapter implements PullRequestStateHistoryRepository {

    private final JpaPullRequestStateHistoryRepository jpaPullRequestStateHistoryRepository;

    @Override
    @Transactional
    public PullRequestStateHistory save(PullRequestStateHistory pullRequestStateHistory) {
        return jpaPullRequestStateHistoryRepository.save(pullRequestStateHistory);
    }
}
