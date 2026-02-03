package com.prism.statistics.domain.reviewcomment.repository;

import com.prism.statistics.domain.reviewcomment.ReviewComment;

import java.util.Optional;

public interface ReviewCommentRepository {

    ReviewComment saveOrFind(ReviewComment reviewComment);

    Optional<ReviewComment> findByGithubCommentId(Long githubCommentId);
}
