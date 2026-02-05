package com.prism.statistics.infrastructure.analysis.metadata.review.persistence;

import com.prism.statistics.domain.analysis.metadata.review.ReviewComment;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface JpaReviewCommentRepository extends ListCrudRepository<ReviewComment, Long> {

    Optional<ReviewComment> findByGithubCommentId(Long githubCommentId);
}
