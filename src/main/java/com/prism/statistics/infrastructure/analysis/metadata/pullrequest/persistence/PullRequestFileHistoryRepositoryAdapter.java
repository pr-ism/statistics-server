package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestFileHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PullRequestFileHistoryRepositoryAdapter implements PullRequestFileHistoryRepository {

    private final JpaPullRequestFileHistoryRepository jpaPullRequestFileHistoryRepository;

    @Override
    @Transactional
    public PullRequestFileHistory save(PullRequestFileHistory pullRequestFileHistory) {
        return jpaPullRequestFileHistoryRepository.save(pullRequestFileHistory);
    }

    @Override
    @Transactional
    public List<PullRequestFileHistory> saveAll(List<PullRequestFileHistory> pullRequestFileHistories) {
        return jpaPullRequestFileHistoryRepository.saveAll(pullRequestFileHistories);
    }
}
