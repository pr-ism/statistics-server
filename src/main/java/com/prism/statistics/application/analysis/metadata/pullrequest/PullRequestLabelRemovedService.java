package com.prism.statistics.application.analysis.metadata.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestLabelRemovedRequest;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestLabelHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestLabelAction;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelHistoryRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelRepository;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.exception.PullRequestNotFoundException;
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
        Long projectId = projectRepository.findIdByApiKey(apiKey)
                .orElseThrow(() -> new InvalidApiKeyException());

        PullRequest pullRequest = pullRequestRepository.findWithLock(projectId, request.pullRequestNumber())
                .orElseThrow(() -> new PullRequestNotFoundException());

        Long pullRequestId = pullRequest.getId();
        String labelName = request.label().name();

        long deleted = pullRequestLabelRepository.deleteLabel(pullRequestId, labelName);

        if (deleted == 0L) {
            return;
        }

        LocalDateTime unlabeledAt = localDateTimeConverter.toLocalDateTime(request.unlabeledAt());

        PullRequestLabelHistory pullRequestLabelHistory = PullRequestLabelHistory.create(
                pullRequestId,
                labelName,
                PullRequestLabelAction.REMOVED,
                unlabeledAt
        );
        pullRequestLabelHistoryRepository.save(pullRequestLabelHistory);
    }
}
