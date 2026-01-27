package com.prism.statistics.application.webhook.event.listener;

import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest.FileData;
import com.prism.statistics.application.webhook.event.PrOpenCreatedEvent;
import com.prism.statistics.domain.pullrequest.PrFile;
import com.prism.statistics.domain.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.pullrequest.repository.PrFileRepository;
import com.prism.statistics.domain.pullrequest.vo.FileChanges;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PrFileEventListener {

    private final PrFileRepository prFileRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PrOpenCreatedEvent event) {
        List<PrFile> prFiles = event.files().stream()
                .map(file -> toPrFile(event.pullRequestId(), file))
                .toList();

        prFileRepository.saveAll(prFiles);
    }

    private PrFile toPrFile(Long pullRequestId, FileData file) {
        return PrFile.create(
                pullRequestId,
                file.filename(),
                FileChangeType.fromGitHubStatus(file.status()),
                FileChanges.create(file.additions(), file.deletions())
        );
    }
}
