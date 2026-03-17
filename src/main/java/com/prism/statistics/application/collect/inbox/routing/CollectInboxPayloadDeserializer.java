package com.prism.statistics.application.collect.inbox.routing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CollectInboxPayloadDeserializer {

    private final ObjectMapper objectMapper;

    public <T> T deserialize(CollectInboxContext context, CollectInboxType type, Class<T> clazz) {
        try {
            return objectMapper.readValue(context.payloadJson(), clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(type + " payload 역직렬화에 실패했습니다.", e);
        }
    }
}
