package com.prism.statistics.application.analysis.metadata.review.event.listener;

import com.prism.statistics.application.analysis.metadata.review.event.RequestedReviewerSavedEvent;
import com.prism.statistics.domain.analysis.metadata.review.RequestedReviewer;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewerAction;
import com.prism.statistics.domain.analysis.metadata.review.history.RequestedReviewerHistory;
import com.prism.statistics.domain.analysis.metadata.review.repository.RequestedReviewerHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RequestedReviewerSavedEventListener {

    private final RequestedReviewerHistoryRepository requestedReviewerHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveHistory(RequestedReviewerSavedEvent event) {
        RequestedReviewerHistory history = createHistory(event.requestedReviewer());
        requestedReviewerHistoryRepository.save(history);
    }

    private RequestedReviewerHistory createHistory(RequestedReviewer reviewer) {
        RequestedReviewerHistory history = RequestedReviewerHistory.create(
                reviewer.getGithubPullRequestId(),
                reviewer.getHeadCommitSha(),
                reviewer.getReviewer(),
                ReviewerAction.REQUESTED,
                reviewer.getGithubRequestedAt()
        );

        history.assignPullRequestId(reviewer.getPullRequestId());
        return history;
    }
}
