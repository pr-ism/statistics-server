package com.prism.statistics.application.analysis.metadata.review;

import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewerAddedRequest;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.analysis.metadata.review.RequestedReviewer;
import com.prism.statistics.domain.analysis.metadata.review.repository.RequestedReviewerRepository;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewerAddedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ProjectRepository projectRepository;
    private final PullRequestRepository pullRequestRepository;
    private final RequestedReviewerRepository requestedReviewerRepository;

    public void addReviewer(String apiKey, ReviewerAddedRequest request) {
        validateApiKey(apiKey);

        RequestedReviewer requestedReviewer = createRequestedReviewer(request);
        requestedReviewerRepository.saveOrFind(requestedReviewer);
    }

    private RequestedReviewer createRequestedReviewer(ReviewerAddedRequest request) {
        GithubUser reviewer = GithubUser.create(request.reviewer().login(), request.reviewer().id());
        LocalDateTime githubRequestedAt = localDateTimeConverter.toLocalDateTime(request.requestedAt());

        RequestedReviewer requestedReviewer = RequestedReviewer.create(
                request.githubPullRequestId(),
                request.pullRequestNumber(),
                request.headCommitSha(),
                reviewer,
                githubRequestedAt
        );

        pullRequestRepository.findIdByGithubId(request.githubPullRequestId())
                .ifPresent(id -> requestedReviewer.assignPullRequestId(id));

        return requestedReviewer;
    }

    private void validateApiKey(String apiKey) {
        if (!projectRepository.existsByApiKey(apiKey)) {
            throw new InvalidApiKeyException();
        }
    }
}
