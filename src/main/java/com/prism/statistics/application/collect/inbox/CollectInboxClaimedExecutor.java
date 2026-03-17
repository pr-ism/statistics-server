package com.prism.statistics.application.collect.inbox;

import com.prism.statistics.application.collect.inbox.routing.CollectInboxContext;
import com.prism.statistics.application.collect.inbox.routing.CollectInboxEventRouter;
import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import com.prism.statistics.infrastructure.collect.inbox.repository.CollectInboxRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollectInboxClaimedExecutor {

    private final Clock clock;
    private final CollectInboxRepository collectInboxRepository;
    private final CollectInboxEventRouter collectInboxEventRouter;
    private final ProcessingSourceContext processingSourceContext;

    @Transactional
    public void execute(CollectInbox claimedInbox) {
        CollectInboxContext context = new CollectInboxContext(
                claimedInbox.getProjectId(),
                claimedInbox.getPayloadJson()
        );
        processingSourceContext.withInboxProcessing(
                () -> collectInboxEventRouter.route(context, claimedInbox.getCollectType())
        );

        claimedInbox.markProcessed(clock.instant());
        collectInboxRepository.save(claimedInbox);
    }
}
