package com.prism.statistics.infrastructure.reviewcomment.persistence;

import com.prism.statistics.domain.reviewcomment.ReviewComment;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface JpaReviewCommentRepository extends ListCrudRepository<ReviewComment, Long> {

    Optional<ReviewComment> findByGithubCommentId(Long githubCommentId);
}
