package com.prism.statistics.application.collect.inbox;

import com.prism.statistics.global.config.properties.CollectInboxProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectInboxWorker {

    private final CollectInboxProperties collectInboxProperties;
    private final CollectInboxProcessor collectInboxProcessor;

    @Scheduled(fixedDelayString = "${app.collect.inbox.poll-delay-ms:200}")
    public void processInbox() {
        if (!collectInboxProperties.workerEnabled()) {
            return;
        }

        try {
            collectInboxProcessor.processPending(collectInboxProperties.batchSize());
        } catch (Exception e) {
            log.error("collect inbox worker 실행에 실패했습니다.", e);
        }
    }
}
