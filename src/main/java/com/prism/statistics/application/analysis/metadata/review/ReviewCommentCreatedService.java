package com.prism.statistics.application.analysis.metadata.review;

import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentCreatedRequest;
import com.prism.statistics.application.collect.inbox.aop.InboxEnqueue;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.review.ReviewComment;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewRepository;
import com.prism.statistics.domain.analysis.metadata.review.enums.CommentSide;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewCommentRepository;
import com.prism.statistics.domain.analysis.metadata.review.vo.CommentLineRange;
import com.prism.statistics.domain.analysis.metadata.review.vo.ParentCommentId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewCommentCreatedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;

    @InboxEnqueue(CollectInboxType.REVIEW_COMMENT_CREATED)
    public void createReviewComment(ReviewCommentCreatedRequest request) {
        ReviewComment reviewComment = buildReviewComment(request);
        reviewCommentRepository.saveOrFind(reviewComment);
    }

    private ReviewComment buildReviewComment(ReviewCommentCreatedRequest request) {
        LocalDateTime githubCreatedAt = localDateTimeConverter.toLocalDateTime(request.createdAt());

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
                .githubCreatedAt(githubCreatedAt)
                .githubUpdatedAt(githubCreatedAt)
                .deleted(false)
                .build();

        reviewRepository.findIdByGithubReviewId(request.githubReviewId())
                .ifPresent(id -> reviewComment.assignReviewId(id));

        return reviewComment;
    }
}
