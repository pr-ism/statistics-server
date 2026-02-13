package com.prism.statistics.application.analysis.metadata.pullrequest.event.listener;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.FileData;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
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
    public void handle(PullRequestOpenCreatedEvent event) {
        List<PullRequestFileHistory> pullRequestFileHistories = event.files().stream()
                .map(file -> toPullRequestFileHistory(event.pullRequestId(), event.headCommitSha(), file, event.githubCreatedAt()))
                .toList();

        pullRequestFileHistoryRepository.saveAll(pullRequestFileHistories);
    }

    private PullRequestFileHistory toPullRequestFileHistory(Long pullRequestId, String headCommitSha, FileData file, LocalDateTime changedAt) {
        return PullRequestFileHistory.create(
                pullRequestId,
                headCommitSha,
                file.filename(),
                FileChangeType.fromGitHubStatus(file.status()),
                FileChanges.create(file.additions(), file.deletions()),
                changedAt
        );
    }
}
