package com.prism.statistics.application.collect.inbox.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.collect.inbox.CollectInboxProcessor;
import com.prism.statistics.application.collect.inbox.ProcessingSourceContext;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CollectInboxEnqueueAspect {

    private final ObjectMapper objectMapper;
    private final ProcessingSourceContext processingSourceContext;
    private final CollectInboxProcessor collectInboxProcessor;

    @Around("@annotation(inboxEnqueue) && args(projectId, request)")
    public Object enqueueWithProjectId(
            ProceedingJoinPoint joinPoint,
            InboxEnqueue inboxEnqueue,
            Long projectId,
            Object request
    ) throws Throwable {
        if (processingSourceContext.isInboxProcessing()) {
            return joinPoint.proceed();
        }

        enqueue(inboxEnqueue.value(), projectId, request);
        return null;
    }

    @Around("@annotation(inboxEnqueue) && args(request)")
    public Object enqueueWithRequest(
            ProceedingJoinPoint joinPoint,
            InboxEnqueue inboxEnqueue,
            Object request
    ) throws Throwable {
        if (processingSourceContext.isInboxProcessing()) {
            return joinPoint.proceed();
        }

        enqueue(inboxEnqueue.value(), null, request);
        return null;
    }

    private void enqueue(CollectInboxType collectType, Long projectId, Object request) {
        try {
            String payloadJson = objectMapper.writeValueAsString(request);
            long runId = extractRunId(request);
            boolean enqueued = collectInboxProcessor.enqueue(collectType, projectId, runId, payloadJson);

            if (!enqueued) {
                log.info("collect inbox enqueue가 중복 요청으로 스킵되었습니다. collectType={}", collectType);
            }
        } catch (Exception e) {
            log.error("collect inbox enqueue 처리 중 예외가 발생했습니다. collectType={}", collectType, e);
            throw new RuntimeException(e);
        }
    }

    private long extractRunId(Object request) {
        try {
            return (long) request.getClass().getMethod("runId").invoke(request);
        } catch (Exception e) {
            throw new IllegalArgumentException("request에서 runId를 추출할 수 없습니다.", e);
        }
    }
}
