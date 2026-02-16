package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.comment.CommentAnalysis;
import com.prism.statistics.domain.analysis.insight.comment.repository.CommentAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentAnalysisRepositoryAdapter implements CommentAnalysisRepository {

    private final JpaCommentAnalysisRepository jpaCommentAnalysisRepository;

    @Override
    @Transactional
    public CommentAnalysis save(CommentAnalysis analysis) {
        return jpaCommentAnalysisRepository.save(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommentAnalysis> findByReviewCommentId(Long reviewCommentId) {
        return jpaCommentAnalysisRepository.findByReviewCommentId(reviewCommentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentAnalysis> findByPullRequestId(Long pullRequestId) {
        return jpaCommentAnalysisRepository.findByPullRequestId(pullRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByReviewCommentId(Long reviewCommentId) {
        return jpaCommentAnalysisRepository.existsByReviewCommentId(reviewCommentId);
    }

    @Override
    @Transactional
    public void deleteByReviewCommentId(Long reviewCommentId) {
        jpaCommentAnalysisRepository.deleteByReviewCommentId(reviewCommentId);
    }
}
