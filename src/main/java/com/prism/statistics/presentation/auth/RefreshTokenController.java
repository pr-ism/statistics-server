package com.prism.statistics.presentation.auth;

import com.prism.statistics.application.auth.GenerateTokenService;
import com.prism.statistics.application.auth.dto.response.TokenResponse;
import com.prism.statistics.global.config.properties.CookieProperties;
import com.prism.statistics.global.config.properties.TokenProperties;
import com.prism.statistics.global.security.constants.TokenCookieName;
import com.prism.statistics.presentation.auth.exception.RefreshTokenNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("token")
@RestController
@RequiredArgsConstructor
public class RefreshTokenController {

    private final TokenProperties tokenProperties;
    private final CookieProperties cookieProperties;
    private final GenerateTokenService generateTokenService;

    @PostMapping("/refresh-token")
    public ResponseEntity<Void> refreshToken(@CookieValue(TokenCookieName.REFRESH_TOKEN) String refreshToken) {
        validateRefreshToken(refreshToken);

        TokenResponse tokenResponse = generateTokenService.refreshToken(refreshToken);
        HttpCookie accessTokenCookie = createCookie(
                TokenCookieName.ACCESS_TOKEN,
                tokenResponse.accessToken(),
                tokenProperties.accessExpiredSeconds()
        );
        HttpCookie refreshTokenCookie = createCookie(
                TokenCookieName.REFRESH_TOKEN,
                tokenResponse.refreshToken(),
                tokenProperties.refreshExpiredSeconds()
        );

        return ResponseEntity.ok()
                             .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                             .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                             .build();
    }

    private void validateRefreshToken(String refreshToken) {
        if (refreshToken.isBlank()) {
            throw new RefreshTokenNotFoundException();
        }
    }

    private HttpCookie createCookie(String name, String value, int maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                             .httpOnly(true)
                             .secure(true)
                             .path(cookieProperties.path())
                             .sameSite(cookieProperties.sameSite())
                             .maxAge(maxAgeSeconds)
                             .build();
    }
}
