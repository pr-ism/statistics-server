package com.prism.statistics.application.analysis.metadata.pullrequest.event.listener;

import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestEarlySynchronizedEvent;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent.CommitData;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestSynchronizedEvent;
import com.prism.statistics.domain.analysis.metadata.pullrequest.Commit;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.CommitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CommitEventListener {

    private final CommitRepository commitRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveInitialCommits(PullRequestOpenCreatedEvent event) {
        Set<String> existingShas = commitRepository.findAllCommitShasByGithubPullRequestId(event.githubPullRequestId());

        List<Commit> commits = event.commits().stream()
                .filter(commitData -> !existingShas.contains(commitData.sha()))
                .map(commitData -> Commit.create(event.pullRequestId(), event.githubPullRequestId(), commitData.sha(), commitData.committedAt()))
                .toList();

        commitRepository.saveAll(commits);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveNewCommits(PullRequestSynchronizedEvent event) {
        List<Commit> commits = event.newCommits().stream()
                .map(commitData -> Commit.create(event.pullRequestId(), event.githubPullRequestId(), commitData.sha(), commitData.committedAt()))
                .toList();

        commitRepository.saveAll(commits);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveEarlyCommits(PullRequestEarlySynchronizedEvent event) {
        List<Commit> commits = event.commits().stream()
                .map(commitData -> Commit.createEarly(event.githubPullRequestId(), commitData.sha(), commitData.committedAt()))
                .toList();

        commitRepository.saveAll(commits);
    }
}
