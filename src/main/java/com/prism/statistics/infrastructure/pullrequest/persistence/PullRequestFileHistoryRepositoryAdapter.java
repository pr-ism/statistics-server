package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequestFileHistory;
import com.prism.statistics.domain.pullrequest.repository.PullRequestFileHistoryRepository;
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
