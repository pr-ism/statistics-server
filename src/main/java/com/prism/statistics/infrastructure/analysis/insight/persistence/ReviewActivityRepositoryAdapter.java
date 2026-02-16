package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.activity.ReviewActivity;
import com.prism.statistics.domain.analysis.insight.activity.repository.ReviewActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewActivityRepositoryAdapter implements ReviewActivityRepository {

    private final JpaReviewActivityRepository jpaReviewActivityRepository;

    @Override
    @Transactional
    public ReviewActivity save(ReviewActivity activity) {
        return jpaReviewActivityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewActivity> findByPullRequestId(Long pullRequestId) {
        return jpaReviewActivityRepository.findByPullRequestId(pullRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPullRequestId(Long pullRequestId) {
        return jpaReviewActivityRepository.existsByPullRequestId(pullRequestId);
    }
}
