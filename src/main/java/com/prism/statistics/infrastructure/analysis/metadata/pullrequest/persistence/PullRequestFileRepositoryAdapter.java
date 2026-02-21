package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequestFile.pullRequestFile;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestFile;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PullRequestFileRepositoryAdapter implements PullRequestFileRepository {

    private final JpaPullRequestFileRepository jpaPullRequestFileRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public PullRequestFile save(PullRequestFile pullRequestFileEntity) {
        return jpaPullRequestFileRepository.save(pullRequestFileEntity);
    }

    @Override
    @Transactional
    public List<PullRequestFile> saveAll(List<PullRequestFile> pullRequestFiles) {
        return jpaPullRequestFileRepository.saveAll(pullRequestFiles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PullRequestFile> findAllByPullRequestId(Long pullRequestId) {
        return queryFactory
                .selectFrom(pullRequestFile)
                .where(pullRequestFile.pullRequestId.eq(pullRequestId))
                .fetch();
    }

    @Override
    @Transactional
    public void deleteAllByPullRequestId(Long pullRequestId) {
        queryFactory
                .delete(pullRequestFile)
                .where(pullRequestFile.pullRequestId.eq(pullRequestId))
                .execute();
    }
}
