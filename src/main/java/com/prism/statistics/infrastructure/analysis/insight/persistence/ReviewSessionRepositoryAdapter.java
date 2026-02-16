package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.review.ReviewSession;
import com.prism.statistics.domain.analysis.insight.review.repository.ReviewSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewSessionRepositoryAdapter implements ReviewSessionRepository {

    private final JpaReviewSessionRepository jpaReviewSessionRepository;

    @Override
    @Transactional
    public ReviewSession save(ReviewSession session) {
        return jpaReviewSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewSession> findByPullRequestId(Long pullRequestId) {
        return jpaReviewSessionRepository.findByPullRequestId(pullRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewSession> findByPullRequestIdAndReviewerUserId(Long pullRequestId, Long reviewerGithubId) {
        return jpaReviewSessionRepository.findByPullRequestIdAndReviewerUserId(pullRequestId, reviewerGithubId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPullRequestIdAndReviewerUserId(Long pullRequestId, Long reviewerGithubId) {
        return jpaReviewSessionRepository.existsByPullRequestIdAndReviewerUserId(pullRequestId, reviewerGithubId);
    }
}
