package com.prism.statistics.application.analysis.metadata.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelRemovedRequest;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestLabelHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestLabelAction;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelHistoryRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PullRequestLabelRemovedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ProjectRepository projectRepository;
    private final PullRequestRepository pullRequestRepository;
    private final PullRequestLabelRepository pullRequestLabelRepository;
    private final PullRequestLabelHistoryRepository pullRequestLabelHistoryRepository;

    @Transactional
    public void removePullRequestLabel(String apiKey, PullRequestLabelRemovedRequest request) {
        validateApiKey(apiKey);

        Long githubPullRequestId = request.githubPullRequestId();
        String headCommitSha = request.headCommitSha();
        String labelName = request.label().name();

        long deleted = pullRequestLabelRepository.deleteLabelByGithubId(githubPullRequestId, labelName);

        if (deleted == 0L) {
            return;
        }

        LocalDateTime unlabeledAt = localDateTimeConverter.toLocalDateTime(request.unlabeledAt());

        PullRequestLabelHistory pullRequestLabelHistory = PullRequestLabelHistory.create(
                githubPullRequestId,
                headCommitSha,
                labelName,
                PullRequestLabelAction.REMOVED,
                unlabeledAt
        );

        pullRequestRepository.findIdByGithubId(githubPullRequestId)
                .ifPresent(id -> pullRequestLabelHistory.assignPullRequestId(id));

        pullRequestLabelHistoryRepository.save(pullRequestLabelHistory);
    }

    private void validateApiKey(String apiKey) {
        if (!projectRepository.existsByApiKey(apiKey)) {
            throw new InvalidApiKeyException();
        }
    }
}
