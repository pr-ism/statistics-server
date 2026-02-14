package com.prism.statistics.application.analysis.metadata.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelAddedRequest;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestLabel;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PullRequestLabelAddedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ProjectRepository projectRepository;
    private final PullRequestRepository pullRequestRepository;
    private final PullRequestLabelRepository pullRequestLabelRepository;

    public void addPullRequestLabel(String apiKey, PullRequestLabelAddedRequest request) {
        validateApiKey(apiKey);

        PullRequestLabel pullRequestLabel = createPullRequestLabel(request);
        pullRequestLabelRepository.saveOrFind(pullRequestLabel);
    }

    private PullRequestLabel createPullRequestLabel(PullRequestLabelAddedRequest request) {
        Long githubPullRequestId = request.githubPullRequestId();
        LocalDateTime githubLabeledAt = localDateTimeConverter.toLocalDateTime(request.labeledAt());

        PullRequestLabel pullRequestLabel = PullRequestLabel.create(
                githubPullRequestId, request.headCommitSha(), request.label().name(), githubLabeledAt
        );

        pullRequestRepository.findIdByGithubId(githubPullRequestId)
                .ifPresent(id -> pullRequestLabel.assignPullRequestId(id));

        return pullRequestLabel;
    }

    private void validateApiKey(String apiKey) {
        if (!projectRepository.existsByApiKey(apiKey)) {
            throw new InvalidApiKeyException();
        }
    }
}
