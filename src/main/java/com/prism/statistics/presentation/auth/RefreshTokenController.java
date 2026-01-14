package com.prism.statistics.presentation.auth;

import com.prism.statistics.application.auth.GenerateTokenService;
import com.prism.statistics.application.auth.dto.response.TokenResponse;
import com.prism.statistics.global.config.properties.TokenProperties;
import com.prism.statistics.presentation.auth.exception.RefreshTokenNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("token")
@RestController
@RequiredArgsConstructor
public class RefreshTokenController {

    private static final String ACCESS_TOKEN_COOKIE_KEY = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_KEY = "refreshToken";

    private final TokenProperties tokenProperties;
    private final GenerateTokenService generateTokenService;

    @PostMapping("/refresh-token")
    public ResponseEntity<Void> refreshToken(HttpServletRequest request) {
        String refreshToken = findRefreshToken(request.getCookies());
        TokenResponse tokenResponse = generateTokenService.refreshToken(refreshToken);
        HttpCookie accessTokenCookie = createCookie(
                ACCESS_TOKEN_COOKIE_KEY,
                tokenResponse.accessToken(),
                tokenProperties.accessExpiredSeconds()
        );
        HttpCookie refreshTokenCookie = createCookie(
                REFRESH_TOKEN_COOKIE_KEY,
                tokenResponse.refreshToken(),
                tokenProperties.refreshExpiredSeconds()
        );

        return ResponseEntity.ok()
                             .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                             .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                             .build();
    }

    private String findRefreshToken(Cookie[] cookies) {
        if (cookies == null) {
            throw new RefreshTokenNotFoundException();
        }

        for (Cookie cookie : cookies) {
            if (REFRESH_TOKEN_COOKIE_KEY.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new RefreshTokenNotFoundException();
    }

    private HttpCookie createCookie(String key, String token, int maxAgeSeconds) {
        return ResponseCookie.from(key, token)
                             .httpOnly(true)
                             .secure(true)
                             .path("/")
                             .sameSite(SameSite.NONE.attributeValue())
                             .maxAge(maxAgeSeconds)
                             .build();
    }
}
