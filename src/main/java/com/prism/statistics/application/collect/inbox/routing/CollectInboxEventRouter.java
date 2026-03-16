package com.prism.statistics.application.collect.inbox.routing;

import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CollectInboxEventRouter {

    private final Map<CollectInboxType, CollectInboxEventHandler> handlers = new EnumMap<>(CollectInboxType.class);

    public CollectInboxEventRouter(List<CollectInboxEventHandler> handlerList) {
        handlerList.forEach(handler -> handlers.put(handler.supportType(), handler));
    }

    public void route(CollectInboxContext context, CollectInboxType collectType) {
        CollectInboxEventHandler handler = handlers.get(collectType);
        if (handler == null) {
            throw new IllegalArgumentException("지원하지 않는 collectType입니다: " + collectType);
        }

        handler.handle(context);
    }
}
