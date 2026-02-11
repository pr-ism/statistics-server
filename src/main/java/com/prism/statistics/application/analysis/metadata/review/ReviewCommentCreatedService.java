package com.prism.statistics.application.analysis.metadata.review;

import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentCreatedRequest;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.analysis.metadata.review.ReviewComment;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewRepository;
import com.prism.statistics.domain.analysis.metadata.review.enums.CommentSide;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewCommentRepository;
import com.prism.statistics.domain.analysis.metadata.review.vo.CommentLineRange;
import com.prism.statistics.domain.analysis.metadata.review.vo.ParentCommentId;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewCommentCreatedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ProjectRepository projectRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;

    public void createReviewComment(String apiKey, ReviewCommentCreatedRequest request) {
        validateApiKey(apiKey);

        ReviewComment reviewComment = createReviewComment(request);
        reviewCommentRepository.saveOrFind(reviewComment);
    }

    private void validateApiKey(String apiKey) {
        if (!projectRepository.existsByApiKey(apiKey)) {
            throw new InvalidApiKeyException();
        }
    }

    private ReviewComment createReviewComment(ReviewCommentCreatedRequest request) {
        LocalDateTime createdAt = localDateTimeConverter.toLocalDateTime(request.createdAt());
        LocalDateTime updatedAt = localDateTimeConverter.toLocalDateTime(request.updatedAt());

        ReviewComment reviewComment = ReviewComment.builder()
                .githubCommentId(request.githubCommentId())
                .githubReviewId(request.githubReviewId())
                .body(request.body())
                .path(request.path())
                .lineRange(CommentLineRange.create(request.startLine(), request.line()))
                .side(CommentSide.from(request.side()))
                .commitSha(request.commitSha())
                .parentCommentId(ParentCommentId.create(request.inReplyToId()))
                .author(GithubUser.create(request.author().login(), request.author().id()))
                .githubCreatedAt(createdAt)
                .githubUpdatedAt(updatedAt)
                .deleted(false)
                .build();

        reviewRepository.findIdByGithubReviewId(request.githubReviewId())
                .ifPresent(id -> reviewComment.assignReviewId(id));

        return reviewComment;
    }
}
