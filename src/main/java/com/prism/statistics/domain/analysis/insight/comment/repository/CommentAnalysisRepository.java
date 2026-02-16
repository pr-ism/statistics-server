package com.prism.statistics.domain.analysis.insight.comment.repository;

import com.prism.statistics.domain.analysis.insight.comment.CommentAnalysis;

import java.util.List;
import java.util.Optional;

public interface CommentAnalysisRepository {

    CommentAnalysis save(CommentAnalysis analysis);

    Optional<CommentAnalysis> findByReviewCommentId(Long reviewCommentId);

    List<CommentAnalysis> findByPullRequestId(Long pullRequestId);

    boolean existsByReviewCommentId(Long reviewCommentId);
}
