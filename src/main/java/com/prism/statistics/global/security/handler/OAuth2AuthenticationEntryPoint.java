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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

@RequiredArgsConstructor
public class OAuth2AuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        PrintWriter writer = response.getWriter();
        AuthExceptionResponse exceptionResponse = new AuthExceptionResponse("로그인이 필요한 기능입니다.");

        writer.println(objectMapper.writeValueAsString(exceptionResponse));
        writer.flush();
    }
}
