package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.review.ReviewSession;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface JpaReviewSessionRepository extends ListCrudRepository<ReviewSession, Long> {

    List<ReviewSession> findByPullRequestId(Long pullRequestId);

    Optional<ReviewSession> findByPullRequestIdAndReviewerUserId(Long pullRequestId, Long reviewerGithubId);

    boolean existsByPullRequestIdAndReviewerUserId(Long pullRequestId, Long reviewerGithubId);
}
