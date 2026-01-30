package com.prism.statistics.application.webhook.event.listener;

import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest.FileData;
import com.prism.statistics.application.webhook.event.PrOpenCreatedEvent;
import com.prism.statistics.domain.pullrequest.PullRequestFileHistory;
import com.prism.statistics.domain.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.pullrequest.repository.PullRequestFileHistoryRepository;
import com.prism.statistics.domain.pullrequest.vo.FileChanges;
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
    public void handle(PrOpenCreatedEvent event) {
        List<PullRequestFileHistory> pullRequestFileHistories = event.files().stream()
                .map(file -> toPullRequestFileHistory(event.pullRequestId(), file, event.prCreatedAt()))
                .toList();

        pullRequestFileHistoryRepository.saveAll(pullRequestFileHistories);
    }

    private PullRequestFileHistory toPullRequestFileHistory(Long pullRequestId, FileData file, LocalDateTime changedAt) {
        return PullRequestFileHistory.create(
                pullRequestId,
                file.filename(),
                FileChangeType.fromGitHubStatus(file.status()),
                FileChanges.create(file.additions(), file.deletions()),
                changedAt
        );
    }
}
