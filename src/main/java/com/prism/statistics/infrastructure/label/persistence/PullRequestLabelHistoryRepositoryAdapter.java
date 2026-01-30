package com.prism.statistics.infrastructure.label.persistence;

import com.prism.statistics.domain.label.PullRequestLabelHistory;
import com.prism.statistics.domain.label.repository.PullRequestLabelHistoryRepository;
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
