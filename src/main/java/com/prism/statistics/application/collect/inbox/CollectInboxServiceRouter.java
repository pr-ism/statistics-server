package com.prism.statistics.application.collect.inbox;

import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Component;

@Component
public class CollectInboxServiceRouter {

    private final Map<CollectInboxType, BiConsumer<Long, String>> routes = new EnumMap<>(CollectInboxType.class);

    public CollectInboxServiceRouter() {
    }

    public void route(CollectInboxType collectType, Long projectId, String payloadJson) {
        BiConsumer<Long, String> handler = routes.get(collectType);
        if (handler == null) {
            throw new IllegalArgumentException("지원하지 않는 collectType입니다: " + collectType);
        }

        handler.accept(projectId, payloadJson);
    }

    protected void register(CollectInboxType collectType, BiConsumer<Long, String> handler) {
        routes.put(collectType, handler);
    }
}
