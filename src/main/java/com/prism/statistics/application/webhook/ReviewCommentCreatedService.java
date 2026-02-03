package com.prism.statistics.application.webhook;

import com.prism.statistics.application.webhook.dto.request.ReviewCommentCreatedRequest;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.reviewcomment.ReviewComment;
import com.prism.statistics.domain.reviewcomment.enums.CommentSide;
import com.prism.statistics.domain.reviewcomment.repository.ReviewCommentRepository;
import com.prism.statistics.domain.reviewcomment.vo.CommentLineRange;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewCommentCreatedService {

    private final Clock clock;
    private final ProjectRepository projectRepository;
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
        LocalDateTime createdAt = toLocalDateTime(request.createdAt());
        LocalDateTime updatedAt = toLocalDateTime(request.updatedAt());

        return ReviewComment.create(
                request.githubCommentId(),
                request.githubReviewId(),
                request.body(),
                request.path(),
                CommentLineRange.create(request.startLine(), request.line()),
                CommentSide.from(request.side()),
                request.commitSha(),
                request.inReplyToId(),
                request.author().login(),
                request.author().id(),
                createdAt,
                updatedAt
        );
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, clock.getZone());
    }
}
