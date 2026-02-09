package com.prism.statistics.infrastructure.analysis.metadata.review.persistence;

import static com.prism.statistics.domain.analysis.metadata.review.QRequestedReviewer.requestedReviewer;

import com.prism.statistics.domain.analysis.metadata.review.RequestedReviewer;
import com.prism.statistics.domain.analysis.metadata.review.repository.RequestedReviewerRepository;
import com.prism.statistics.infrastructure.common.MysqlDuplicateKeyDetector;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.exception.RequestedReviewerNotFoundException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RequestedReviewerRepositoryAdapter implements RequestedReviewerRepository {

    private final RequestedReviewerCreator requestedReviewerCreator;
    private final JpaRequestedReviewerRepository jpaRequestedReviewerRepository;
    private final JPAQueryFactory queryFactory;
    private final MysqlDuplicateKeyDetector duplicateKeyDetector;

    @Override
    public RequestedReviewer saveOrFind(RequestedReviewer reviewer) {
        try {
            requestedReviewerCreator.saveNew(reviewer);
            return reviewer;
        } catch (DataIntegrityViolationException ex) {
            if (duplicateKeyDetector.isDuplicateKey(ex)) {
                return jpaRequestedReviewerRepository.findByGithubPullRequestIdAndReviewerUserId(
                        reviewer.getGithubPullRequestId(), reviewer.getReviewer().getUserId()
                ).orElseThrow(() -> new RequestedReviewerNotFoundException());
            }
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long pullRequestId, Long githubUid) {
        return queryFactory
                .selectOne()
                .from(requestedReviewer)
                .where(
                        requestedReviewer.pullRequestId.eq(pullRequestId),
                        requestedReviewer.reviewer.userId.eq(githubUid)
                )
                .fetchFirst() != null;
    }

    @Override
    @Transactional
    public long delete(Long pullRequestId, Long githubUid) {
        return queryFactory
                .delete(requestedReviewer)
                .where(
                        requestedReviewer.pullRequestId.eq(pullRequestId),
                        requestedReviewer.reviewer.userId.eq(githubUid)
                )
                .execute();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RequestedReviewer> findByGithubPullRequestIdAndUserId(
            Long githubPullRequestId, Long userId
    ) {
        return jpaRequestedReviewerRepository.findByGithubPullRequestIdAndReviewerUserId(
                githubPullRequestId, userId
        );
    }

    @Override
    @Transactional
    public long deleteByGithubId(Long githubPullRequestId, Long userId) {
        return queryFactory
                .delete(requestedReviewer)
                .where(
                        requestedReviewer.githubPullRequestId.eq(githubPullRequestId),
                        requestedReviewer.reviewer.userId.eq(userId)
                )
                .execute();
    }
}
