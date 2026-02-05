package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestLabelHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PullRequestLabelHistoryRepositoryAdapter implements PullRequestLabelHistoryRepository {

    private final JpaPullRequestLabelHistoryRepository jpaPullRequestLabelHistoryRepository;

    @Override
    @Transactional
    public PullRequestLabelHistory save(PullRequestLabelHistory pullRequestLabelHistory) {
        return jpaPullRequestLabelHistoryRepository.save(pullRequestLabelHistory);
    }
}
