package com.prism.statistics.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.global.security.handler.dto.response.AuthExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

@RequiredArgsConstructor
public class OAuth2AccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.FORBIDDEN.value());

        PrintWriter writer = response.getWriter();
        AuthExceptionResponse authExceptionResponse = new AuthExceptionResponse("권한이 없습니다.");

        writer.println(objectMapper.writeValueAsString(authExceptionResponse));
        writer.flush();
    }
}
