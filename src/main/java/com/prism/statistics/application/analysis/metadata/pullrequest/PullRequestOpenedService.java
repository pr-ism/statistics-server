package com.prism.statistics.application.analysis.metadata.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.CommitNode;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.PullRequestData;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestSavedEvent;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent.CommitData;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
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
            createDraftPullRequest(projectId, request);
            return;
        }
        createOpenPullRequest(projectId, request);
    }

    private void createDraftPullRequest(Long projectId, PullRequestOpenedRequest request) {
        PullRequestData pullRequestData = request.pullRequest();
        LocalDateTime githubCreatedAt = localDateTimeConverter.toLocalDateTime(pullRequestData.createdAt());

        PullRequest savedPullRequest = savePullRequest(projectId, pullRequestData, PullRequestState.DRAFT, PullRequestTiming.createDraft(githubCreatedAt));

        publishPullRequestSavedEvent(savedPullRequest);
        publishPullRequestCreatedEvent(savedPullRequest, projectId, pullRequestData, request, PullRequestState.DRAFT, githubCreatedAt);
    }

    private void createOpenPullRequest(Long projectId, PullRequestOpenedRequest request) {
        PullRequestData pullRequestData = request.pullRequest();
        LocalDateTime githubCreatedAt = localDateTimeConverter.toLocalDateTime(pullRequestData.createdAt());

        PullRequest savedPullRequest = savePullRequest(projectId, pullRequestData, PullRequestState.OPEN, PullRequestTiming.createOpen(githubCreatedAt));

        publishPullRequestSavedEvent(savedPullRequest);
        publishPullRequestCreatedEvent(savedPullRequest, projectId, pullRequestData, request, PullRequestState.OPEN, githubCreatedAt);
    }

    private PullRequest savePullRequest(Long projectId, PullRequestData pullRequestData, PullRequestState state, PullRequestTiming timing) {
        PullRequest pullRequest = PullRequest.builder()
                .githubPullRequestId(pullRequestData.githubPullRequestId())
                .projectId(projectId)
                .author(GithubUser.create(pullRequestData.author().login(), pullRequestData.author().id()))
                .pullRequestNumber(pullRequestData.number())
                .headCommitSha(pullRequestData.headCommitSha())
                .title(pullRequestData.title())
                .state(state)
                .link(pullRequestData.url())
                .changeStats(PullRequestChangeStats.create(
                        pullRequestData.changedFiles(),
                        pullRequestData.additions(),
                        pullRequestData.deletions()
                ))
                .commitCount(pullRequestData.commits().totalCount())
                .timing(timing)
                .build();

        return pullRequestRepository.save(pullRequest);
    }

    private void publishPullRequestSavedEvent(PullRequest savedPullRequest) {
        eventPublisher.publishEvent(new PullRequestSavedEvent(
                savedPullRequest.getGithubPullRequestId(), savedPullRequest.getId()
        ));
    }

    private void publishPullRequestCreatedEvent(
            PullRequest savedPullRequest,
            Long projectId,
            PullRequestData pullRequestData,
            PullRequestOpenedRequest request,
            PullRequestState initialState,
            LocalDateTime githubCreatedAt
    ) {
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
                pullRequestData.headCommitSha(),
                initialState,
                pullRequestChangeStats,
                pullRequestData.commits().totalCount(),
                githubCreatedAt,
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
