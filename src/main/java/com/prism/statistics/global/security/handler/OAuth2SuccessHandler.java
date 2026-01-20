package com.prism.statistics.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.auth.GenerateTokenService;
import com.prism.statistics.application.auth.SocialLoginService;
import com.prism.statistics.application.auth.dto.LoggedInUserDto;
import com.prism.statistics.application.auth.dto.response.TokenResponse;
import com.prism.statistics.global.config.properties.TokenProperties;
import com.prism.statistics.global.security.handler.exception.InvalidResponseWriteException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    public static final String DOMAIN = "/";
    public static final String REFRESH_TOKEN_KEY = "refreshToken";
    public static final String ACCESS_TOKEN_KEY = "accessToken";

    private final ObjectMapper objectMapper;
    private final TokenProperties tokenProperties;
    private final SocialLoginService socialLoginService;
    private final GenerateTokenService generateTokenService;
    private final AuthenticationFailureHandler authenticationFailureHandler;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String socialId = (String) oAuth2User.getAttributes()
                                             .get(StandardClaimNames.SUB);
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        try {
            LoggedInUserDto loggedInUserDto = socialLoginService.login(registrationId, socialId);
            TokenResponse tokenResponse = generateTokenService.generate(loggedInUserDto.id());

            addCookie(response, ACCESS_TOKEN_KEY, tokenResponse.accessToken(), tokenProperties.accessExpiredSeconds());
            addCookie(response, REFRESH_TOKEN_KEY, tokenResponse.refreshToken(), tokenProperties.refreshExpiredSeconds());

            writeResponse(response, loggedInUserDto);
        } catch (AuthenticationException exception) {
            authenticationFailureHandler.onAuthenticationFailure(request, response, exception);
        }
    }

    private void writeResponse(HttpServletResponse response, LoggedInUserDto loggedInUserDto) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.CREATED.value());

        try {
            PrintWriter writer = response.getWriter();
            LoginSuccessResponse body = new LoginSuccessResponse(
                    loggedInUserDto.id(),
                    loggedInUserDto.nickname(),
                    loggedInUserDto.isSignUp()
            );

            writer.write(objectMapper.writeValueAsString(body));
            writer.flush();
        } catch (IOException e) {
            throw new InvalidResponseWriteException(e);
        }
    }

    private void addCookie(HttpServletResponse response, String name, String value, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                                              .path(DOMAIN)
                                              .secure(true)
                                              .httpOnly(true)
                                              .sameSite("None")
                                              .maxAge(maxAge)
                                              .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private record LoginSuccessResponse(Long userId, String nickname, boolean signUp) {
    }
}
