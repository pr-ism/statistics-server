package com.prism.statistics.application.analysis.metadata.review;

import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentDeletedRequest;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewCommentRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.exception.ReviewCommentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewCommentDeletedService {

    private final Clock clock;
    private final ProjectRepository projectRepository;
    private final ReviewCommentRepository reviewCommentRepository;

    public void deleteReviewComment(String apiKey, ReviewCommentDeletedRequest request) {
        validateApiKey(apiKey);
        validateReviewCommentExists(request.githubCommentId());

        reviewCommentRepository.softDeleteIfLatest(
                request.githubCommentId(),
                toLocalDateTime(request.updatedAt())
        );
    }

    private void validateApiKey(String apiKey) {
        if (!projectRepository.existsByApiKey(apiKey)) {
            throw new InvalidApiKeyException();
        }
    }

    private void validateReviewCommentExists(Long githubCommentId) {
        if (!reviewCommentRepository.existsByGithubCommentId(githubCommentId)) {
            throw new ReviewCommentNotFoundException();
        }
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, clock.getZone());
    }
}
