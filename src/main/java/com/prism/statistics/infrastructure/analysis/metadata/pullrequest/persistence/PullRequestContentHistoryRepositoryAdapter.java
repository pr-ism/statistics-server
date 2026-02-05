package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestContentHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestContentHistoryRepository;
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
