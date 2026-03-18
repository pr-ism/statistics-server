package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;

public interface CollectInboxEventHandler {

    CollectInboxType supportType();

    void handle(CollectInboxContext context);
}
