package com.prism.statistics.application.analysis.metadata.review;

import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewerAddedRequest;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.analysis.metadata.review.RequestedReviewer;
import com.prism.statistics.domain.analysis.metadata.review.history.RequestedReviewerHistory;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewerAction;
import com.prism.statistics.domain.analysis.metadata.review.repository.RequestedReviewerHistoryRepository;
import com.prism.statistics.domain.analysis.metadata.review.repository.RequestedReviewerRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewerAddedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ProjectRepository projectRepository;
    private final RequestedReviewerRepository requestedReviewerRepository;
    private final RequestedReviewerHistoryRepository requestedReviewerHistoryRepository;

    public void addReviewer(String apiKey, ReviewerAddedRequest request) {
        if (!projectRepository.existsByApiKey(apiKey)) {
            throw new InvalidApiKeyException();
        }

        GithubUser reviewer = GithubUser.create(request.reviewer().login(), request.reviewer().id());
        LocalDateTime requestedAt = localDateTimeConverter.toLocalDateTime(request.requestedAt());

        RequestedReviewer requestedReviewer = RequestedReviewer.create(
                request.githubPullRequestId(),
                request.headCommitSha(),
                reviewer,
                requestedAt
        );

        RequestedReviewer saved = requestedReviewerRepository.saveOrFind(requestedReviewer);

        if (!saved.equals(requestedReviewer)) {
            return;
        }

        RequestedReviewerHistory requestedReviewerHistory = RequestedReviewerHistory.create(
                requestedReviewer.getGithubPullRequestId(),
                request.headCommitSha(),
                reviewer,
                ReviewerAction.REQUESTED,
                requestedAt
        );
        requestedReviewerHistoryRepository.save(requestedReviewerHistory);
    }
}
