package com.prism.statistics.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.prism.statistics.application.auth.dto.response.TokenResponse;
import com.prism.statistics.domain.auth.TokenDecoder;
import com.prism.statistics.domain.auth.TokenEncoder;
import com.prism.statistics.domain.auth.TokenScheme;
import com.prism.statistics.domain.auth.TokenType;
import com.prism.statistics.global.config.TokenConfig;
import com.prism.statistics.global.config.properties.TokenProperties;
import com.prism.statistics.infrastructure.auth.jwt.JwsSignerFinder;
import com.prism.statistics.infrastructure.auth.jwt.JwsVerifierFinder;
import com.prism.statistics.infrastructure.auth.jwt.exception.InvalidTokenException;
import java.time.Clock;
import java.time.LocalDateTime;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenerateTokenServiceTest {

    private TokenEncoder tokenEncoder;
    private GenerateTokenService generateTokenService;

    @BeforeEach
    void setUp() throws Exception {
        TokenProperties tokenProperties = new TokenProperties(
                "thisIsA32ByteAccessTokenKeyForHS",
                "thisIsA32ByteRefreshTokenKeyForH",
                "thisIsA32ByteEncryptionKeyForAES",
                "test-issuer",
                3_600,
                259_200,
                3_600_000L,
                259_200_000L
        );
        Clock clock = Clock.systemUTC();
        TokenConfig tokenConfig = new TokenConfig(clock, tokenProperties);

        SecretKey accessTokenSecretKey = tokenConfig.accessTokenSecretKey();
        SecretKey refreshTokenSecretKey = tokenConfig.refreshTokenSecretKey();
        SecretKeySpec aesSecretKey = tokenConfig.aesSecretKey();

        JwsSignerFinder signerFinder = tokenConfig.jwsSignerFinder(accessTokenSecretKey, refreshTokenSecretKey);
        JwsVerifierFinder verifierFinder = tokenConfig.jwsVerifierFinder(accessTokenSecretKey, refreshTokenSecretKey);
        JWEEncrypter jweEncrypter = tokenConfig.jweEncrypter(aesSecretKey);
        JWEDecrypter jweDecrypter = tokenConfig.jweDecrypter(aesSecretKey);

        this.tokenEncoder = tokenConfig.tokenEncoder(jweEncrypter, signerFinder);
        TokenDecoder tokenDecoder = tokenConfig.tokenDecoder(jweDecrypter, verifierFinder);
        this.generateTokenService = new GenerateTokenService(clock, tokenDecoder, tokenEncoder);
    }

    @Test
    void 유효한_userId로_토큰을_생성할_수_있다() {
        // when
        TokenResponse actual = generateTokenService.generate(1L);

        // then
        assertAll(
                () -> assertThat(actual.accessToken()).isNotNull(),
                () -> assertThat(actual.refreshToken()).isNotNull(),
                () -> assertThat(actual.tokenScheme()).isEqualTo(TokenScheme.BEARER.name())
        );
    }

    @Test
    void 유효한_refreshToken으로_토큰을_재발급_받을_수_있다() {
        // given
        String refreshToken = tokenEncoder.encode(LocalDateTime.now(), TokenType.REFRESH, 1L);

        // when
        TokenResponse actual = generateTokenService.refreshToken(refreshToken);

        // then
        assertAll(
                () -> assertThat(actual.accessToken()).isNotNull(),
                () -> assertThat(actual.refreshToken()).isNotNull(),
                () -> assertThat(actual.tokenScheme()).isEqualTo(TokenScheme.BEARER.name())
        );
    }

    @Test
    void 유효하지_않은_refreshToken으로는_토큰을_재발급_받을_수_없다() {
        // given
        String invalidRefreshToken = tokenEncoder.encode(LocalDateTime.now(), TokenType.ACCESS, 1L);

        // when & then
        assertThatThrownBy(() -> generateTokenService.refreshToken(invalidRefreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("위변조된 토큰입니다.");
    }
}
