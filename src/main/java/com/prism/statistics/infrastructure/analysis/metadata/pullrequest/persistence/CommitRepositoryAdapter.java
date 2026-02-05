package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.Commit;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.CommitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommitRepositoryAdapter implements CommitRepository {

    private final JpaCommitRepository jpaCommitRepository;

    @Override
    public Commit save(Commit commit) {
        return jpaCommitRepository.save(commit);
    }

    @Override
    public List<Commit> saveAll(List<Commit> commits) {
        return jpaCommitRepository.saveAll(commits);
    }
}
