package com.prism.statistics.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.auth.GenerateTokenService;
import com.prism.statistics.application.auth.SocialLoginService;
import com.prism.statistics.application.auth.dto.LoggedInUserDto;
import com.prism.statistics.application.auth.dto.response.TokenResponse;
import com.prism.statistics.global.config.properties.CookieProperties;
import com.prism.statistics.global.config.properties.TokenProperties;
import com.prism.statistics.global.security.constants.TokenCookieName;
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
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final TokenProperties tokenProperties;
    private final CookieProperties cookieProperties;
    private final SocialLoginService socialLoginService;
    private final GenerateTokenService generateTokenService;
    private final AuthenticationFailureHandler authenticationFailureHandler;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        try {
            String socialId = extractSocialId(authentication);
            String registrationId = extractRegistrationId(authentication);

            LoggedInUserDto loggedInUserDto = socialLoginService.login(registrationId, socialId);

            TokenResponse tokenResponse = generateTokens(loggedInUserDto.id());
            writeResponse(response, loggedInUserDto, tokenResponse);
        } catch (AuthenticationException exception) {
            authenticationFailureHandler.onAuthenticationFailure(request, response, exception);
        }
    }

    private String extractRegistrationId(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
            throw new AuthenticationServiceException("인증에 실패했습니다.");
        }

        return oauth2Token.getAuthorizedClientRegistrationId();
    }

    private String extractSocialId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof OidcUser oidcUser)) {
            throw new AuthenticationServiceException("OIDC 사용자 정보가 없습니다.");
        }
        return oidcUser.getSubject();
    }

    private TokenResponse generateTokens(Long userId) {
        return generateTokenService.generate(userId);
    }

    private void writeResponse(HttpServletResponse response, LoggedInUserDto loggedInUserDto, TokenResponse tokenResponse) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.CREATED.value());

        addCookie(
                response,
                TokenCookieName.ACCESS_TOKEN,
                tokenResponse.accessToken(),
                tokenProperties.accessExpiredSeconds()
        );
        addCookie(
                response,
                TokenCookieName.REFRESH_TOKEN,
                tokenResponse.refreshToken(),
                tokenProperties.refreshExpiredSeconds()
        );

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
                                              .path(cookieProperties.path())
                                              .secure(true)
                                              .httpOnly(true)
                                              .sameSite(cookieProperties.sameSite())
                                              .maxAge(maxAge)
                                              .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private record LoginSuccessResponse(Long userId, String nickname, boolean signUp) {
    }
}
