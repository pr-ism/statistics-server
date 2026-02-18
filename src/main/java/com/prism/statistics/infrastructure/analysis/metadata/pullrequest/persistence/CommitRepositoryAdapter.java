package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QCommit.commit;

import com.prism.statistics.domain.analysis.metadata.pullrequest.Commit;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.CommitRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommitRepositoryAdapter implements CommitRepository {

    private final JpaCommitRepository jpaCommitRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Commit save(Commit commitEntity) {
        return jpaCommitRepository.save(commitEntity);
    }

    @Override
    public List<Commit> saveAll(List<Commit> commits) {
        return jpaCommitRepository.saveAll(commits);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllCommitShasByPullRequestId(Long pullRequestId) {
        return queryFactory
                .select(commit.commitSha)
                .from(commit)
                .where(commit.pullRequestId.eq(pullRequestId))
                .fetch();
    }
}
