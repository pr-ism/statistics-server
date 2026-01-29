package com.prism.statistics.application.webhook;

import com.prism.statistics.application.webhook.dto.request.LabelAddedRequest;
import com.prism.statistics.domain.label.PrLabel;
import com.prism.statistics.domain.label.PrLabelHistory;
import com.prism.statistics.domain.label.enums.LabelAction;
import com.prism.statistics.domain.label.repository.PrLabelHistoryRepository;
import com.prism.statistics.domain.label.repository.PrLabelRepository;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.pullrequest.PullRequest;
import com.prism.statistics.domain.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import com.prism.statistics.infrastructure.pullrequest.persistence.exception.PullRequestNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LabelAddedService {

    private final Clock clock;
    private final ProjectRepository projectRepository;
    private final PullRequestRepository pullRequestRepository;
    private final PrLabelRepository prLabelRepository;
    private final PrLabelHistoryRepository prLabelHistoryRepository;

    @Transactional
    public void addLabel(String apiKey, LabelAddedRequest request) {
        Long projectId = projectRepository.findIdByApiKey(apiKey)
                .orElseThrow(() -> new ProjectNotFoundException());

        PullRequest pullRequest = pullRequestRepository.findPullRequest(projectId, request.prNumber())
                .orElseThrow(() -> new PullRequestNotFoundException());

        String labelName = request.label().name();
        Long pullRequestId = pullRequest.getId();

        if (prLabelRepository.exists(pullRequestId, labelName)) {
            return;
        }

        LocalDateTime labeledAt = toLocalDateTime(request.labeledAt());

        PrLabel prLabel = PrLabel.create(pullRequestId, labelName, labeledAt);
        try {
            prLabelRepository.save(prLabel);
        } catch (DataIntegrityViolationException e) {
            return;
        }

        PrLabelHistory prLabelHistory = PrLabelHistory.create(
                pullRequest.getId(),
                labelName,
                LabelAction.ADDED,
                labeledAt
        );
        prLabelHistoryRepository.save(prLabelHistory);
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, clock.getZone());
    }
}
