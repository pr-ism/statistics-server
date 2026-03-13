package com.prism.statistics.application.collect.inbox.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.collect.inbox.CollectInboxProcessor;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CollectInboxEnqueueAspect {

    private static final String RUN_ID_HEADER = "X-GitHub-Run-Id";

    private final ObjectMapper objectMapper;
    private final ProjectRepository projectRepository;
    private final CollectInboxProcessor collectInboxProcessor;

    @Around("@annotation(inboxEnqueue) && args(apiKey, request)")
    public Object enqueue(ProceedingJoinPoint joinPoint, InboxEnqueue inboxEnqueue, String apiKey, Object request) {
        CollectInboxType collectType = inboxEnqueue.value();
        String idempotencyKey = extractIdempotencyKey();

        Long projectId = projectRepository.findIdByApiKey(apiKey)
                .orElse(null);

        if (projectId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String payloadJson = objectMapper.writeValueAsString(request);
            boolean enqueued = collectInboxProcessor.enqueue(collectType, projectId, idempotencyKey, payloadJson);

            if (!enqueued) {
                log.info("collect inbox enqueue가 중복 요청으로 스킵되었습니다. collectType={}", collectType);
            }
        } catch (Exception e) {
            log.error("collect inbox enqueue 처리 중 예외가 발생했습니다. collectType={}", collectType, e);
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok().build();
    }

    private String extractIdempotencyKey() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new IllegalStateException("HTTP 요청 컨텍스트를 찾을 수 없습니다.");
        }

        HttpServletRequest request = attributes.getRequest();
        String runId = request.getHeader(RUN_ID_HEADER);

        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("X-GitHub-Run-Id 헤더가 비어있습니다.");
        }

        return runId;
    }
}
