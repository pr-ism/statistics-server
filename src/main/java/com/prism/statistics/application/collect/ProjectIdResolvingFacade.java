package com.prism.statistics.application.collect;

import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestClosedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestConvertedToDraftService;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestOpenedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestReadyForReviewService;
import com.prism.statistics.application.analysis.metadata.pullrequest.PullRequestReopenedService;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestClosedRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestConvertedToDraftRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReadyForReviewRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReopenedRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectIdResolvingFacade {

    private final ProjectApiKeyService projectApiKeyService;
    private final PullRequestOpenedService pullRequestOpenedService;
    private final PullRequestClosedService pullRequestClosedService;
    private final PullRequestReadyForReviewService pullRequestReadyForReviewService;
    private final PullRequestReopenedService pullRequestReopenedService;
    private final PullRequestConvertedToDraftService pullRequestConvertedToDraftService;

    public void createPullRequest(String apiKey, PullRequestOpenedRequest request) {
        Long projectId = projectApiKeyService.resolveProjectId(apiKey);
        pullRequestOpenedService.createPullRequest(projectId, request);
    }

    public void closePullRequest(String apiKey, PullRequestClosedRequest request) {
        Long projectId = projectApiKeyService.resolveProjectId(apiKey);
        pullRequestClosedService.closePullRequest(projectId, request);
    }

    public void readyForReview(String apiKey, PullRequestReadyForReviewRequest request) {
        Long projectId = projectApiKeyService.resolveProjectId(apiKey);
        pullRequestReadyForReviewService.readyForReview(projectId, request);
    }

    public void reopenPullRequest(String apiKey, PullRequestReopenedRequest request) {
        Long projectId = projectApiKeyService.resolveProjectId(apiKey);
        pullRequestReopenedService.reopenPullRequest(projectId, request);
    }

    public void convertToDraft(String apiKey, PullRequestConvertedToDraftRequest request) {
        Long projectId = projectApiKeyService.resolveProjectId(apiKey);
        pullRequestConvertedToDraftService.convertToDraft(projectId, request);
    }
}
