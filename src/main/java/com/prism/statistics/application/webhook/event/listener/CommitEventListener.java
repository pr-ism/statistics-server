package com.prism.statistics.application.webhook.event.listener;

import com.prism.statistics.application.webhook.event.PrOpenCreatedEvent;
import com.prism.statistics.application.webhook.event.PrOpenCreatedEvent.CommitData;
import com.prism.statistics.domain.pullrequest.Commit;
import com.prism.statistics.domain.pullrequest.repository.CommitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommitEventListener {

    private final CommitRepository commitRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PrOpenCreatedEvent event) {
        List<Commit> commits = event.commits().stream()
                .map(commitData -> toCommit(event.pullRequestId(), commitData))
                .toList();

        commitRepository.saveAll(commits);
    }

    private Commit toCommit(Long pullRequestId, CommitData commitData) {
        return Commit.create(
                pullRequestId,
                commitData.sha(),
                commitData.committedAt()
        );
    }
}
