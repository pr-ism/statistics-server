package com.prism.statistics.infrastructure.pullrequest.persistence;

import static com.prism.statistics.domain.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.pullrequest.PullRequest;
import com.prism.statistics.domain.pullrequest.repository.PullRequestRepository;
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
    public Optional<PullRequest> findPullRequest(Long projectId, int prNumber) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(pullRequest)
                        .where(
                                pullRequest.projectId.eq(projectId),
                                pullRequest.prNumber.eq(prNumber)
                        )
                        .fetchOne()
        );
    }

    @Override
    @Transactional
    public Optional<PullRequest> findWithLock(Long projectId, int prNumber) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(pullRequest)
                        .where(
                                pullRequest.projectId.eq(projectId),
                                pullRequest.prNumber.eq(prNumber)
                        )
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
                .orderBy(pullRequest.prNumber.desc())
                .fetch();
    }
}
