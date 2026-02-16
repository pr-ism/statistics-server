package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.review.ReviewResponseTime;
import com.prism.statistics.domain.analysis.insight.review.repository.ReviewResponseTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewResponseTimeRepositoryAdapter implements ReviewResponseTimeRepository {

    private final JpaReviewResponseTimeRepository jpaReviewResponseTimeRepository;

    @Override
    @Transactional
    public ReviewResponseTime save(ReviewResponseTime responseTime) {
        return jpaReviewResponseTimeRepository.save(responseTime);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewResponseTime> findByPullRequestId(Long pullRequestId) {
        return jpaReviewResponseTimeRepository.findByPullRequestId(pullRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPullRequestId(Long pullRequestId) {
        return jpaReviewResponseTimeRepository.existsByPullRequestId(pullRequestId);
    }
}
