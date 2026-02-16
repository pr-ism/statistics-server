package com.prism.statistics.domain.analysis.insight.review.repository;

import com.prism.statistics.domain.analysis.insight.review.ReviewSession;

import java.util.List;
import java.util.Optional;

public interface ReviewSessionRepository {

    ReviewSession save(ReviewSession session);

    List<ReviewSession> findByPullRequestId(Long pullRequestId);

    Optional<ReviewSession> findByPullRequestIdAndReviewerUserId(Long pullRequestId, Long reviewerGithubId);

    boolean existsByPullRequestIdAndReviewerUserId(Long pullRequestId, Long reviewerGithubId);
}
