package com.prism.statistics.application.webhook;

import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest;
import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest.CommitNode;
import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest.PullRequestData;
import com.prism.statistics.application.webhook.event.PrDraftCreatedEvent;
import com.prism.statistics.application.webhook.event.PrOpenCreatedEvent;
import com.prism.statistics.application.webhook.event.PrOpenCreatedEvent.CommitData;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.pullrequest.PullRequest;
import com.prism.statistics.domain.pullrequest.enums.PrState;
import com.prism.statistics.domain.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.pullrequest.vo.PrTiming;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrOpenedService {

    private final Clock clock;
    private final ProjectRepository projectRepository;
    private final PullRequestRepository pullRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void handle(String apiKey, PrOpenedRequest request) {
        Long projectId = projectRepository.findIdByApiKey(apiKey)
                .orElseThrow(() -> new InvalidApiKeyException());

        if (request.isDraft()) {
            eventPublisher.publishEvent(new PrDraftCreatedEvent(request));
            return;
        }
        PullRequestData prData = request.pullRequest();

        PullRequest savedPullRequest = savePullRequest(projectId, prData);

        publishPrOpenCreatedEvent(savedPullRequest, projectId, prData, request);
    }

    private PullRequest savePullRequest(Long projectId, PullRequestData prData) {
        LocalDateTime prCreatedAt = toLocalDateTime(prData.createdAt());

        PullRequestChangeStats pullRequestChangeStats = PullRequestChangeStats.create(
                prData.changedFiles(),
                prData.additions(),
                prData.deletions()
        );

        PrTiming timing = PrTiming.createOpen(prCreatedAt);

        PullRequest pullRequest = PullRequest.opened(
                projectId,
                prData.author().login(),
                prData.number(),
                prData.title(),
                prData.url(),
                pullRequestChangeStats,
                prData.commits().totalCount(),
                timing
        );

        return pullRequestRepository.save(pullRequest);
    }

    private void publishPrOpenCreatedEvent(
            PullRequest savedPullRequest,
            Long projectId,
            PullRequestData prData,
            PrOpenedRequest request
    ) {
        LocalDateTime prCreatedAt = toLocalDateTime(prData.createdAt());
        PullRequestChangeStats pullRequestChangeStats = PullRequestChangeStats.create(
                prData.changedFiles(),
                prData.additions(),
                prData.deletions()
        );

        List<CommitData> commits = prData.commits().nodes().stream()
                .map(node -> toCommitData(node))
                .toList();

        PrOpenCreatedEvent event = new PrOpenCreatedEvent(
                savedPullRequest.getId(),
                projectId,
                PrState.OPEN,
                pullRequestChangeStats,
                prData.commits().totalCount(),
                prCreatedAt,
                request.files(),
                commits
        );

        eventPublisher.publishEvent(event);
    }

    private CommitData toCommitData(CommitNode node) {
        return new CommitData(
                node.commit().oid(),
                toLocalDateTime(node.commit().committedDate())
        );
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, clock.getZone());
    }
}
