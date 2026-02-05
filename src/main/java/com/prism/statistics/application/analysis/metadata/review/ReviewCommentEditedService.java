package com.prism.statistics.application.analysis.metadata.review;

import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentEditedRequest;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewCommentRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.exception.ReviewCommentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewCommentEditedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ProjectRepository projectRepository;
    private final ReviewCommentRepository reviewCommentRepository;

    public void editReviewComment(String apiKey, ReviewCommentEditedRequest request) {
        validateApiKey(apiKey);
        validateReviewCommentExists(request.githubCommentId());

        reviewCommentRepository.updateBodyIfLatest(
                request.githubCommentId(),
                request.body(),
                localDateTimeConverter.toLocalDateTime(request.updatedAt())
        );
    }

    private void validateReviewCommentExists(Long githubCommentId) {
        if (!reviewCommentRepository.existsByGithubCommentId(githubCommentId)) {
            throw new ReviewCommentNotFoundException();
        }
    }

    private void validateApiKey(String apiKey) {
        if (!projectRepository.existsByApiKey(apiKey)) {
            throw new InvalidApiKeyException();
        }
    }
}
