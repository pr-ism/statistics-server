package com.prism.statistics.application.analysis.metadata.review;

import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewerAddedRequest;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.analysis.metadata.review.RequestedReviewer;
import com.prism.statistics.domain.analysis.metadata.review.history.RequestedReviewerHistory;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewerAction;
import com.prism.statistics.domain.analysis.metadata.review.repository.RequestedReviewerHistoryRepository;
import com.prism.statistics.domain.analysis.metadata.review.repository.RequestedReviewerRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.exception.PullRequestNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewerAddedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ProjectRepository projectRepository;
    private final PullRequestRepository pullRequestRepository;
    private final RequestedReviewerRepository requestedReviewerRepository;
    private final RequestedReviewerHistoryRepository requestedReviewerHistoryRepository;

    @Transactional
    public void addReviewer(String apiKey, ReviewerAddedRequest request) {
        Long projectId = projectRepository.findIdByApiKey(apiKey)
                .orElseThrow(() -> new InvalidApiKeyException());

        PullRequest pullRequest = pullRequestRepository.findWithLock(projectId, request.pullRequestNumber())
                .orElseThrow(() -> new PullRequestNotFoundException());

        Long pullRequestId = pullRequest.getId();
        Long githubUid = request.reviewer().id();

        if (requestedReviewerRepository.exists(pullRequestId, githubUid)) {
            return;
        }

        String githubMention = request.reviewer().login();
        LocalDateTime requestedAt = localDateTimeConverter.toLocalDateTime(request.requestedAt());

        RequestedReviewer requestedReviewer = RequestedReviewer.create(
                pullRequestId,
                githubMention,
                githubUid,
                requestedAt
        );

        requestedReviewerRepository.save(requestedReviewer);

        RequestedReviewerHistory requestedReviewerHistory = RequestedReviewerHistory.create(
                pullRequestId,
                githubMention,
                githubUid,
                ReviewerAction.REQUESTED,
                requestedAt
        );
        requestedReviewerHistoryRepository.save(requestedReviewerHistory);
    }
}
