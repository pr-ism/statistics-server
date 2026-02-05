package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestStateHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestStateHistoryRepository;
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
