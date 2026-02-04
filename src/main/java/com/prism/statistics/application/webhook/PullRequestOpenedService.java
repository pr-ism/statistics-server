package com.prism.statistics.application.webhook;

import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest;
import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest.CommitNode;
import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest.PullRequestData;
import com.prism.statistics.application.webhook.event.PullRequestDraftCreatedEvent;
import com.prism.statistics.application.webhook.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.application.webhook.event.PullRequestOpenCreatedEvent.CommitData;
import com.prism.statistics.application.webhook.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.pullrequest.PullRequest;
import com.prism.statistics.domain.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PullRequestOpenedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ProjectRepository projectRepository;
    private final PullRequestRepository pullRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createPullRequest(String apiKey, PullRequestOpenedRequest request) {
        Long projectId = projectRepository.findIdByApiKey(apiKey)
                .orElseThrow(() -> new InvalidApiKeyException());

        if (request.isDraft()) {
            eventPublisher.publishEvent(new PullRequestDraftCreatedEvent(request));
            return;
        }

        PullRequestData pullRequestData = request.pullRequest();

        PullRequest savedPullRequest = savePullRequest(projectId, pullRequestData);

        publishPullRequestOpenCreatedEvent(savedPullRequest, projectId, pullRequestData, request);
    }

    private PullRequest savePullRequest(Long projectId, PullRequestData pullRequestData) {
        LocalDateTime pullRequestCreatedAt = localDateTimeConverter.toLocalDateTime(pullRequestData.createdAt());

        PullRequestChangeStats pullRequestChangeStats = PullRequestChangeStats.create(
                pullRequestData.changedFiles(),
                pullRequestData.additions(),
                pullRequestData.deletions()
        );

        PullRequestTiming pullRequestTiming = PullRequestTiming.createOpen(pullRequestCreatedAt);

        PullRequest pullRequest = PullRequest.opened(
                projectId,
                pullRequestData.author().login(),
                pullRequestData.number(),
                pullRequestData.title(),
                pullRequestData.url(),
                pullRequestChangeStats,
                pullRequestData.commits().totalCount(),
                pullRequestTiming
        );

        return pullRequestRepository.save(pullRequest);
    }

    private void publishPullRequestOpenCreatedEvent(
            PullRequest savedPullRequest,
            Long projectId,
            PullRequestData pullRequestData,
            PullRequestOpenedRequest request
    ) {
        LocalDateTime pullRequestCreatedAt = localDateTimeConverter.toLocalDateTime(pullRequestData.createdAt());
        PullRequestChangeStats pullRequestChangeStats = PullRequestChangeStats.create(
                pullRequestData.changedFiles(),
                pullRequestData.additions(),
                pullRequestData.deletions()
        );

        List<CommitData> commits = pullRequestData.commits().nodes().stream()
                .map(node -> toCommitData(node))
                .toList();

        PullRequestOpenCreatedEvent event = new PullRequestOpenCreatedEvent(
                savedPullRequest.getId(),
                projectId,
                PullRequestState.OPEN,
                pullRequestChangeStats,
                pullRequestData.commits().totalCount(),
                pullRequestCreatedAt,
                request.files(),
                commits
        );

        eventPublisher.publishEvent(event);
    }

    private CommitData toCommitData(CommitNode node) {
        return new CommitData(
                node.commit().oid(),
                localDateTimeConverter.toLocalDateTime(node.commit().committedDate())
        );
    }
}
