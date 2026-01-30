package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequestHistory;
import com.prism.statistics.domain.pullrequest.repository.PullRequestHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PullRequestHistoryRepositoryAdapter implements PullRequestHistoryRepository {

    private final JpaPullRequestHistoryRepository pullRequestHistoryRepository;

    @Override
    public PullRequestHistory save(PullRequestHistory prChangeHistory) {
        return pullRequestHistoryRepository.save(prChangeHistory);
    }
}
