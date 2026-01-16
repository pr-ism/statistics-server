package com.prism.statistics.application.auth;

import com.prism.statistics.application.auth.dto.response.TokenResponse;
import com.prism.statistics.domain.auth.PrivateClaims;
import com.prism.statistics.domain.auth.TokenDecoder;
import com.prism.statistics.domain.auth.TokenEncoder;
import com.prism.statistics.domain.auth.TokenScheme;
import com.prism.statistics.domain.auth.TokenType;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("token")
@Service
@RequiredArgsConstructor
public class GenerateTokenService {

    private final Clock clock;
    private final TokenDecoder tokenDecoder;
    private final TokenEncoder tokenEncoder;

    public TokenResponse generate(Long userId) {
        LocalDateTime now = LocalDateTime.now(clock);
        String accessToken = tokenEncoder.encode(now, TokenType.ACCESS, userId);
        String refreshToken = tokenEncoder.encode(now, TokenType.REFRESH, userId);

        return new TokenResponse(accessToken, refreshToken, TokenScheme.BEARER.name());
    }

    public TokenResponse refreshToken(String refreshToken) {
        PrivateClaims privateClaims = tokenDecoder.decode(TokenType.REFRESH, refreshToken);

        return generate(privateClaims.userId());
    }
}
