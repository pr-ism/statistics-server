package com.prism.statistics.domain.analysis.metadata.review.repository;

import com.prism.statistics.domain.analysis.metadata.review.ReviewComment;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReviewCommentRepository {

    ReviewComment saveOrFind(ReviewComment reviewComment);

    Optional<ReviewComment> findByGithubCommentId(Long githubCommentId);

    boolean existsByGithubCommentId(Long githubCommentId);

    long updateBodyIfLatest(Long githubCommentId, String body, LocalDateTime updatedAt);

    long softDeleteIfLatest(Long githubCommentId, LocalDateTime updatedAt);
}
