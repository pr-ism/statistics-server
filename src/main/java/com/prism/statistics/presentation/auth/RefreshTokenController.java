package com.prism.statistics.presentation.auth;

import com.prism.statistics.application.auth.GenerateTokenService;
import com.prism.statistics.application.auth.dto.response.TokenResponse;
import com.prism.statistics.global.config.properties.CookieProperties;
import com.prism.statistics.global.config.properties.TokenProperties;
import com.prism.statistics.global.security.constants.TokenCookieName;
import com.prism.statistics.presentation.auth.dto.response.AuthorizationResponse;
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
    public ResponseEntity<AuthorizationResponse> refreshToken(@CookieValue(TokenCookieName.REFRESH_TOKEN) String refreshToken) {
        validateRefreshToken(refreshToken);

        TokenResponse tokenResponse = generateTokenService.refreshToken(refreshToken);
        HttpCookie refreshTokenCookie = createCookie(
                tokenResponse.refreshToken(),
                tokenProperties.refreshExpiredSeconds()
        );

        return ResponseEntity.ok()
                             .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                             .header(HttpHeaders.CACHE_CONTROL, "no-store")
                             .header(HttpHeaders.PRAGMA, "no-cache")
                             .body(new AuthorizationResponse("Bearer " + tokenResponse.accessToken()));
    }

    private void validateRefreshToken(String refreshToken) {
        if (refreshToken.isBlank()) {
            throw new RefreshTokenNotFoundException();
        }
    }

    private HttpCookie createCookie(String value, int maxAgeSeconds) {
        return ResponseCookie.from(TokenCookieName.REFRESH_TOKEN, value)
                             .httpOnly(true)
                             .secure(true)
                             .path(cookieProperties.path())
                             .sameSite(cookieProperties.sameSite())
                             .maxAge(maxAgeSeconds)
                             .build();
    }
}
