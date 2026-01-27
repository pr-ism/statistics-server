package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequest;
import com.prism.statistics.domain.pullrequest.repository.PullRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PullRequestRepositoryAdapter implements PullRequestRepository {

    private final JpaPullRequestRepository jpaPullRequestRepository;

    @Override
    public PullRequest save(PullRequest pullRequest) {
        return jpaPullRequestRepository.save(pullRequest);
    }
}
