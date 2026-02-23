package com.prism.statistics.application.analysis.metadata.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest.CommitNode;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestSynchronizedEvent;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent.CommitData;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.CommitRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.exception.HeadCommitNotFoundException;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestSynchronizedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ProjectRepository projectRepository;
    private final PullRequestRepository pullRequestRepository;
    private final CommitRepository commitRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void synchronizePullRequest(String apiKey, PullRequestSynchronizedRequest request) {
        Long projectId = projectRepository.findIdByApiKey(apiKey)
                .orElseThrow(() -> new InvalidApiKeyException());

        pullRequestRepository.findPullRequest(projectId, request.pullRequestNumber())
                .ifPresentOrElse(
                        pullRequest -> processSynchronize(pullRequest, request),
                        () -> log.warn("PullRequest를 찾을 수 없습니다. pullRequestNumber={}", request.pullRequestNumber())
                );
    }

    private void processSynchronize(PullRequest pullRequest, PullRequestSynchronizedRequest request) {
        if (pullRequest.isNotSynchronizable()) {
            log.warn("이미 닫힌 pull request에 synchronize 이벤트가 도착했습니다. pullRequestId={}, state={}", pullRequest.getId(), pullRequest.getState());
            return;
        }

        List<CommitNode> newCommitNodes = filterNewCommits(pullRequest.getId(), request);
        boolean isNewer = isNewer(pullRequest.getHeadCommitSha(), request);
        PullRequestChangeStats changeStats = PullRequestChangeStats.create(
                request.changedFiles(),
                request.additions(),
                request.deletions()
        );

        if (isNewer) {
            pullRequest.synchronize(request.headCommitSha(), changeStats, request.commits().totalCount());
        }

        publishSynchronizedEvent(pullRequest, request, newCommitNodes, isNewer, changeStats);
    }

    private boolean isNewer(String currentHeadCommitSha, PullRequestSynchronizedRequest request) {
        return request.commits().nodes().stream()
                .anyMatch(node -> node.sha().equals(currentHeadCommitSha));
    }

    private List<CommitNode> filterNewCommits(Long pullRequestId, PullRequestSynchronizedRequest request) {
        Set<String> existingShas = commitRepository.findAllCommitShasByPullRequestId(pullRequestId);

        return request.commits().nodes().stream()
                .filter(node -> !existingShas.contains(node.sha()))
                .toList();
    }

    private void publishSynchronizedEvent(
            PullRequest pullRequest,
            PullRequestSynchronizedRequest request,
            List<CommitNode> newCommitNodes,
            boolean isNewer,
            PullRequestChangeStats changeStats
    ) {
        LocalDateTime githubChangedAt = findHeadCommitDate(request);

        List<CommitData> newCommits = newCommitNodes.stream()
                .map(node -> new CommitData(node.sha(), localDateTimeConverter.toLocalDateTime(node.committedDate())))
                .toList();

        eventPublisher.publishEvent(new PullRequestSynchronizedEvent(
                pullRequest.getId(),
                request.headCommitSha(),
                isNewer,
                changeStats,
                request.commits().totalCount(),
                githubChangedAt,
                request.files(),
                newCommits
        ));
    }

    private LocalDateTime findHeadCommitDate(PullRequestSynchronizedRequest request) {
        return request.commits().nodes().stream()
                .filter(node -> node.sha().equals(request.headCommitSha()))
                .findFirst()
                .map(node -> localDateTimeConverter.toLocalDateTime(node.committedDate()))
                .orElseThrow(() -> new HeadCommitNotFoundException());
    }
}
