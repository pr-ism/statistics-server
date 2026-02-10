package com.prism.statistics.application.analysis.metadata.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelAddedRequest;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestLabel;
import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestLabelHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestLabelAction;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelHistoryRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelRepository;
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
    private final PullRequestLabelRepository pullRequestLabelRepository;
    private final PullRequestLabelHistoryRepository pullRequestLabelHistoryRepository;

    public void addPullRequestLabel(String apiKey, PullRequestLabelAddedRequest request) {
        validateApiKey(apiKey);

        Long githubPullRequestId = request.githubPullRequestId();
        String headCommitSha = request.headCommitSha();
        String labelName = request.label().name();
        LocalDateTime labeledAt = localDateTimeConverter.toLocalDateTime(request.labeledAt());

        PullRequestLabel pullRequestLabel = PullRequestLabel.create(
                githubPullRequestId, headCommitSha, labelName, labeledAt
        );

        PullRequestLabel saved = pullRequestLabelRepository.saveOrFind(pullRequestLabel);

        if (!saved.equals(pullRequestLabel)) {
            return;
        }

        PullRequestLabelHistory pullRequestLabelHistory = PullRequestLabelHistory.create(
                githubPullRequestId,
                null,
                headCommitSha,
                labelName,
                PullRequestLabelAction.ADDED,
                labeledAt
        );

        pullRequestLabelHistoryRepository.save(pullRequestLabelHistory);
    }

    private void validateApiKey(String apiKey) {
        if (!projectRepository.existsByApiKey(apiKey)) {
            throw new InvalidApiKeyException();
        }
    }
}
