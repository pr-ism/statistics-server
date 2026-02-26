package com.prism.statistics.application.analysis.metadata.pullrequest.event.listener;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest.FileData;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestEarlySynchronizedEvent;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestSynchronizedEvent;
import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestFileHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileHistoryRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.FileChanges;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PullRequestFileHistoryEventListener {

    private final PullRequestFileHistoryRepository pullRequestFileHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveInitialFileHistory(PullRequestOpenCreatedEvent event) {
        List<PullRequestFileHistory> pullRequestFileHistories = event.files().stream()
                .map(file -> PullRequestFileHistory.create(
                        event.pullRequestId(),
                        event.githubPullRequestId(),
                        event.headCommitSha(),
                        file.filename(),
                        FileChangeType.fromGitHubStatus(file.status()),
                        FileChanges.create(file.additions(), file.deletions()),
                        event.githubCreatedAt()
                ))
                .toList();

        pullRequestFileHistoryRepository.saveAllInBatch(pullRequestFileHistories);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveFileHistory(PullRequestSynchronizedEvent event) {
        List<PullRequestFileHistory> pullRequestFileHistories = event.files().stream()
                .map(file -> toFileHistory(event.pullRequestId(), event.githubPullRequestId(), event.headCommitSha(), file, event.githubChangedAt()))
                .toList();

        pullRequestFileHistoryRepository.saveAllInBatch(pullRequestFileHistories);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveEarlyFileHistory(PullRequestEarlySynchronizedEvent event) {
        List<PullRequestFileHistory> pullRequestFileHistories = event.files().stream()
                .map(file -> toEarlyFileHistory(event.githubPullRequestId(), event.headCommitSha(), file, event.githubChangedAt()))
                .toList();

        pullRequestFileHistoryRepository.saveAllInBatch(pullRequestFileHistories);
    }

    private PullRequestFileHistory toFileHistory(Long pullRequestId, Long githubPullRequestId, String headCommitSha, FileData file, LocalDateTime githubChangedAt) {
        if (file.previousFilename() != null) {
            return PullRequestFileHistory.createRenamed(
                    pullRequestId,
                    githubPullRequestId,
                    headCommitSha,
                    file.filename(),
                    file.previousFilename(),
                    FileChanges.create(file.additions(), file.deletions()),
                    githubChangedAt
            );
        }

        return PullRequestFileHistory.create(
                pullRequestId,
                githubPullRequestId,
                headCommitSha,
                file.filename(),
                FileChangeType.fromGitHubStatus(file.status()),
                FileChanges.create(file.additions(), file.deletions()),
                githubChangedAt
        );
    }

    private PullRequestFileHistory toEarlyFileHistory(Long githubPullRequestId, String headCommitSha, FileData file, LocalDateTime githubChangedAt) {
        if (file.previousFilename() != null) {
            return PullRequestFileHistory.createEarlyRenamed(
                    githubPullRequestId,
                    headCommitSha,
                    file.filename(),
                    file.previousFilename(),
                    FileChanges.create(file.additions(), file.deletions()),
                    githubChangedAt
            );
        }

        return PullRequestFileHistory.createEarly(
                githubPullRequestId,
                headCommitSha,
                file.filename(),
                FileChangeType.fromGitHubStatus(file.status()),
                FileChanges.create(file.additions(), file.deletions()),
                githubChangedAt
        );
    }
}
