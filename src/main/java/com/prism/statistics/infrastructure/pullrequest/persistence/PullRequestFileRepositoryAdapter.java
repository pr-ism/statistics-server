package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PullRequestFile;
import com.prism.statistics.domain.pullrequest.repository.PullRequestFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PullRequestFileRepositoryAdapter implements PullRequestFileRepository {

    private final JpaPullRequestFileRepository jpaPullRequestFileRepository;

    @Override
    @Transactional
    public PullRequestFile save(PullRequestFile pullRequestFile) {
        return jpaPullRequestFileRepository.save(pullRequestFile);
    }

    @Override
    @Transactional
    public List<PullRequestFile> saveAll(List<PullRequestFile> pullRequestFiles) {
        return jpaPullRequestFileRepository.saveAll(pullRequestFiles);
    }
}
