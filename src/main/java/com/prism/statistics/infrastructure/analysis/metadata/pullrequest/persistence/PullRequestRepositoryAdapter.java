package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PullRequestRepositoryAdapter implements PullRequestRepository {

    private final JpaPullRequestRepository jpaPullRequestRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public PullRequest save(PullRequest pr) {
        return jpaPullRequestRepository.save(pr);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PullRequest> findById(Long id) {
        return jpaPullRequestRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PullRequest> findPullRequest(Long projectId, int pullRequestNumber) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(pullRequest)
                        .where(
                                pullRequest.projectId.eq(projectId),
                                pullRequest.pullRequestNumber.eq(pullRequestNumber)
                        )
                        .fetchOne()
        );
    }

    @Override
    @Transactional
    public Optional<PullRequest> findWithLock(Long githubPullRequestId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(pullRequest)
                        .where(pullRequest.githubPullRequestId.eq(githubPullRequestId))
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .fetchOne()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PullRequest> findAllByProjectId(Long projectId) {
        return queryFactory
                .selectFrom(pullRequest)
                .where(pullRequest.projectId.eq(projectId))
                .orderBy(pullRequest.pullRequestNumber.desc())
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findIdByGithubId(Long githubPullRequestId) {
        return Optional.ofNullable(
                queryFactory
                        .select(pullRequest.id)
                        .from(pullRequest)
                        .where(pullRequest.githubPullRequestId.eq(githubPullRequestId))
                        .fetchOne()
        );
    }
}
