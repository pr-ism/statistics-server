package com.prism.statistics.application.analysis.metadata.pullrequest.event.listener;

import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestLabelSavedEvent;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestLabel;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestLabelAction;
import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestLabelHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PullRequestLabelSavedEventListener {

    private final PullRequestLabelHistoryRepository pullRequestLabelHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveHistory(PullRequestLabelSavedEvent event) {
        PullRequestLabel label = event.pullRequestLabel();

        PullRequestLabelHistory history = PullRequestLabelHistory.create(
                label.getGithubPullRequestId(),
                label.getHeadCommitSha(),
                label.getLabelName(),
                PullRequestLabelAction.ADDED,
                label.getLabeledAt()
        );

        if (label.getPullRequestId() != null) {
            history.assignPullRequestId(label.getPullRequestId());
        }

        pullRequestLabelHistoryRepository.save(history);
    }
}
