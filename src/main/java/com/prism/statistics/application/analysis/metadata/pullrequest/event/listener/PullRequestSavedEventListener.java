package com.prism.statistics.application.analysis.metadata.pullrequest.event.listener;

import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestSavedEvent;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelHistoryRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelRepository;
import com.prism.statistics.domain.analysis.metadata.review.repository.RequestedReviewerHistoryRepository;
import com.prism.statistics.domain.analysis.metadata.review.repository.RequestedReviewerRepository;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PullRequestSavedEventListener {

    private final PullRequestLabelRepository pullRequestLabelRepository;
    private final PullRequestLabelHistoryRepository pullRequestLabelHistoryRepository;
    private final RequestedReviewerRepository requestedReviewerRepository;
    private final RequestedReviewerHistoryRepository requestedReviewerHistoryRepository;
    private final ReviewRepository reviewRepository;

    @Async("backfillExecutor")
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void backfillPullRequestLabel(PullRequestSavedEvent event) {
        pullRequestLabelRepository.backfillPullRequestId(event.githubPullRequestId(), event.pullRequestId());
    }

    @Async("backfillExecutor")
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void backfillPullRequestLabelHistory(PullRequestSavedEvent event) {
        pullRequestLabelHistoryRepository.backfillPullRequestId(event.githubPullRequestId(), event.pullRequestId());
    }

    @Async("backfillExecutor")
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void backfillRequestedReviewer(PullRequestSavedEvent event) {
        requestedReviewerRepository.backfillPullRequestId(event.githubPullRequestId(), event.pullRequestId());
    }

    @Async("backfillExecutor")
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void backfillRequestedReviewerHistory(PullRequestSavedEvent event) {
        requestedReviewerHistoryRepository.backfillPullRequestId(event.githubPullRequestId(), event.pullRequestId());
    }

    @Async("backfillExecutor")
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void backfillReview(PullRequestSavedEvent event) {
        reviewRepository.backfillPullRequestId(event.githubPullRequestId(), event.pullRequestId());
    }
}
