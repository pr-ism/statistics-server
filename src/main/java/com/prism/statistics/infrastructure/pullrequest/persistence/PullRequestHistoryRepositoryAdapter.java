package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequestHistory;
import com.prism.statistics.domain.pullrequest.repository.PullRequestHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PullRequestHistoryRepositoryAdapter implements PullRequestHistoryRepository {

    private final JpaPullRequestHistoryRepository pullRequestHistoryRepository;

    @Override
    @Transactional
    public PullRequestHistory save(PullRequestHistory pullRequestHistory) {
        return pullRequestHistoryRepository.save(pullRequestHistory);
    }
}
