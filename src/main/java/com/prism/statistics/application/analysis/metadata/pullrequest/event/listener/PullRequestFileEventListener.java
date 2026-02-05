package com.prism.statistics.application.analysis.metadata.pullrequest.event.listener;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.FileData;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
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
    public void handle(PullRequestOpenCreatedEvent event) {
        List<PullRequestFile> pullRequestFiles = event.files().stream()
                .map(file -> toPullRequestFile(event.pullRequestId(), file))
                .toList();

        pullRequestFileRepository.saveAll(pullRequestFiles);
    }

    private PullRequestFile toPullRequestFile(Long pullRequestId, FileData file) {
        return PullRequestFile.create(
                pullRequestId,
                file.filename(),
                FileChangeType.fromGitHubStatus(file.status()),
                FileChanges.create(file.additions(), file.deletions())
        );
    }
}
