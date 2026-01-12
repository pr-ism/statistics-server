package com.prism.statistics.infrastructure.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.crypto.AESEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.prism.statistics.domain.auth.TokenType;
import com.prism.statistics.global.config.properties.TokenProperties;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JwtEncoderTest {

    TokenProperties tokenProperties = new TokenProperties(
            "thisIsA32ByteAccessTokenKeyForHS",
            "thisIsA32ByteRefreshTokenKeyForH",
            "thisIsA32ByteEncryptionKeyForAES",
            "issuer",
            43_200,
            259_200,
            43_200_000L,
            259_200_000L
    );

    JwtEncoder jwtEncoder;

    @BeforeEach
    void beforeEach() throws JOSEException {
        byte[] encryptionKeyBytes = "24ByteEncryptionKeyForJWE".getBytes(StandardCharsets.UTF_8);
        SecretKey encryptionSecretKey = new SecretKeySpec(encryptionKeyBytes, 0, 24, "AES");
        JWEEncrypter jweEncrypter = new AESEncrypter(encryptionSecretKey);

        byte[] accessTokenKeyBytes = tokenProperties.accessKey().getBytes(StandardCharsets.UTF_8);
        SecretKey accessTokenSecretKey = new SecretKeySpec(accessTokenKeyBytes, "HmacSHA256");
        MACSigner accessTokenSigner = new MACSigner(accessTokenSecretKey);

        byte[] refreshTokenKeyBytes = tokenProperties.refreshKey().getBytes(StandardCharsets.UTF_8);
        SecretKey refreshTokenSecretKey = new SecretKeySpec(refreshTokenKeyBytes, "HmacSHA256");
        MACSigner refreshTokenSigner = new MACSigner(refreshTokenSecretKey);

        JwsSignerFinder jwsSignerFinder = new JwsSignerFinder(accessTokenSigner, refreshTokenSigner);

        jwtEncoder = new JwtEncoder(jweEncrypter, jwsSignerFinder, tokenProperties);
    }

    @ParameterizedTest
    @EnumSource(value = TokenType.class)
    void 토큰을_인코딩한다(TokenType tokenType) {
        // when
        String actual = jwtEncoder.encode(LocalDateTime.now(), tokenType, 1L);

        // then
        assertThat(actual).isNotBlank();
    }
}
