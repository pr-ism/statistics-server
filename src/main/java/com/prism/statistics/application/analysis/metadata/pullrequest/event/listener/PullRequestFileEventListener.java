package com.prism.statistics.application.analysis.metadata.pullrequest.event.listener;

import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestEarlySynchronizedEvent;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestSynchronizedEvent;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestFile;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.FileChanges;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PullRequestFileEventListener {

    private final PullRequestFileRepository pullRequestFileRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveInitialFiles(PullRequestOpenCreatedEvent event) {
        if (pullRequestFileRepository.existsByGithubPullRequestId(event.githubPullRequestId())) {
            return;
        }

        List<PullRequestFile> pullRequestFiles = event.files().stream()
                .map(file -> PullRequestFile.create(
                        event.pullRequestId(),
                        event.githubPullRequestId(),
                        file.filename(),
                        FileChangeType.fromGitHubStatus(file.status()),
                        FileChanges.create(file.additions(), file.deletions())
                ))
                .toList();

        pullRequestFileRepository.saveAllInBatch(pullRequestFiles);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void updateFiles(PullRequestSynchronizedEvent event) {
        if (!event.isNewer()) {
            return;
        }

        pullRequestFileRepository.deleteAllByPullRequestId(event.pullRequestId());

        List<PullRequestFile> pullRequestFiles = event.files().stream()
                .map(file -> PullRequestFile.create(
                        event.pullRequestId(),
                        event.githubPullRequestId(),
                        file.filename(),
                        FileChangeType.fromGitHubStatus(file.status()),
                        FileChanges.create(file.additions(), file.deletions())
                ))
                .toList();

        pullRequestFileRepository.saveAllInBatch(pullRequestFiles);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveEarlyFiles(PullRequestEarlySynchronizedEvent event) {
        List<PullRequestFile> pullRequestFiles = event.files().stream()
                .map(file -> PullRequestFile.createEarly(
                        event.githubPullRequestId(),
                        file.filename(),
                        FileChangeType.fromGitHubStatus(file.status()),
                        FileChanges.create(file.additions(), file.deletions())
                ))
                .toList();

        pullRequestFileRepository.saveAllInBatch(pullRequestFiles);
    }
}
