package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequestContentHistory;
import com.prism.statistics.domain.pullrequest.repository.PullRequestContentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PullRequestContentHistoryRepositoryAdapter implements PullRequestContentHistoryRepository {

    private final JpaPullRequestContentHistoryRepository jpaPullRequestContentHistoryRepository;

    @Override
    @Transactional
    public PullRequestContentHistory save(PullRequestContentHistory pullRequestContentHistory) {
        return jpaPullRequestContentHistoryRepository.save(pullRequestContentHistory);
    }
}
