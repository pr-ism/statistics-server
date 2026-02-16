package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.review.ReviewSession;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface JpaReviewSessionRepository extends ListCrudRepository<ReviewSession, Long> {

    List<ReviewSession> findByPullRequestId(Long pullRequestId);
}
