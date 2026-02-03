package com.prism.statistics.domain.reviewcomment.repository;

import com.prism.statistics.domain.reviewcomment.ReviewComment;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReviewCommentRepository {

    ReviewComment saveOrFind(ReviewComment reviewComment);

    Optional<ReviewComment> findByGithubCommentId(Long githubCommentId);

    boolean existsByGithubCommentId(Long githubCommentId);

    long updateBodyIfLatest(Long githubCommentId, String body, LocalDateTime updatedAt);
}
