package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.comment.CommentAnalysis;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface JpaCommentAnalysisRepository extends ListCrudRepository<CommentAnalysis, Long> {

    Optional<CommentAnalysis> findByReviewCommentId(Long reviewCommentId);

    List<CommentAnalysis> findByPullRequestId(Long pullRequestId);

    boolean existsByReviewCommentId(Long reviewCommentId);

    void deleteByReviewCommentId(Long reviewCommentId);
}
