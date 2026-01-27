package com.prism.statistics.application.webhook.event.listener;

import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest.FileData;
import com.prism.statistics.application.webhook.event.PrOpenCreatedEvent;
import com.prism.statistics.domain.pullrequest.PrFileHistory;
import com.prism.statistics.domain.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.pullrequest.repository.PrFileHistoryRepository;
import com.prism.statistics.domain.pullrequest.vo.FileChanges;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PrFileHistoryEventListener {

    private final PrFileHistoryRepository prFileHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PrOpenCreatedEvent event) {
        List<PrFileHistory> prFileHistories = event.files().stream()
                .map(file -> toPrFileHistory(event.pullRequestId(), file, event.prCreatedAt()))
                .toList();

        prFileHistoryRepository.saveAll(prFileHistories);
    }

    private PrFileHistory toPrFileHistory(Long pullRequestId, FileData file, LocalDateTime changedAt) {
        return PrFileHistory.create(
                pullRequestId,
                file.filename(),
                FileChangeType.fromGitHubStatus(file.status()),
                FileChanges.create(file.additions(), file.deletions()),
                changedAt
        );
    }
}
