package com.prism.statistics.infrastructure.analysis.metadata.review.persistence;

import com.prism.statistics.domain.analysis.metadata.review.RequestedReviewer;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface JpaRequestedReviewerRepository extends ListCrudRepository<RequestedReviewer, Long> {

    Optional<RequestedReviewer> findByGithubPullRequestIdAndReviewerUserId(
            Long githubPullRequestId, Long userId
    );
}
