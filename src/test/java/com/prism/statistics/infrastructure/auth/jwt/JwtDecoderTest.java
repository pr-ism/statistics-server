package com.prism.statistics.infrastructure.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.AESEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.prism.statistics.domain.auth.PrivateClaims;
import com.prism.statistics.domain.auth.TokenType;
import com.prism.statistics.global.config.properties.TokenProperties;
import com.prism.statistics.infrastructure.auth.jwt.exception.ExpiredTokenException;
import com.prism.statistics.infrastructure.auth.jwt.exception.InvalidTokenException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JwtDecoderTest {

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
    JwtDecoder jwtDecoder;
    JwtEncoder jwtEncoder;
    JWEEncrypter jweEncrypter;
    JwsSignerFinder jwsSignerFinder;
    Clock clock;

    @BeforeEach
    void beforeEach() throws JOSEException {
        clock = Clock.system(ZoneId.of("Asia/Seoul"));

        byte[] encryptionKeyBytes = tokenProperties.encryptionKey().getBytes(StandardCharsets.UTF_8);
        SecretKey encryptionSecretKey = new SecretKeySpec(encryptionKeyBytes, "AES");

        JWEDecrypter jweDecrypter = new AESDecrypter(encryptionSecretKey);
        jweEncrypter = new AESEncrypter(encryptionSecretKey);

        byte[] accessTokenKeyBytes = tokenProperties.accessKey().getBytes(StandardCharsets.UTF_8);
        SecretKey accessTokenSecretKey = new SecretKeySpec(accessTokenKeyBytes, "HmacSHA256");
        JWSVerifier accessTokenJwsVerifier = new MACVerifier(accessTokenSecretKey);
        JWSSigner accessTokenJwsSigner = new MACSigner(accessTokenSecretKey);

        byte[] refreshTokenKeyBytes = tokenProperties.refreshKey().getBytes(StandardCharsets.UTF_8);
        SecretKey refreshTokenSecretKey = new SecretKeySpec(refreshTokenKeyBytes, "HmacSHA256");
        JWSVerifier refreshTokenJwsVerifier = new MACVerifier(refreshTokenSecretKey);
        JWSSigner refreshTokenJwsSigner = new MACSigner(refreshTokenSecretKey);

        JwsVerifierFinder jwsVerifierFinder = new JwsVerifierFinder(accessTokenJwsVerifier, refreshTokenJwsVerifier);

        jwsSignerFinder = new JwsSignerFinder(accessTokenJwsSigner, refreshTokenJwsSigner);
        jwtDecoder = new JwtDecoder(clock, jweDecrypter, jwsVerifierFinder, tokenProperties);
        jwtEncoder = new JwtEncoder(jweEncrypter, jwsSignerFinder, tokenProperties, clock);
    }

    @ParameterizedTest
    @EnumSource(value = TokenType.class)
    void 유효하지_않은_길이의_토큰은_디코딩_할_수_없다(TokenType tokenType) {
        // when & then
        assertThatThrownBy(() -> jwtDecoder.decode(tokenType, "invalid"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("유효한 토큰이 아닙니다.");
    }

    @ParameterizedTest
    @EnumSource(value = TokenType.class)
    void 만료된_토큰은_디코딩_할_수_없다(TokenType tokenType) {
        // given
        String token = jwtEncoder.encode(
                LocalDateTime.of(2022, 2, 2, 13, 13),
                tokenType,
                1L
        );

        // when & then
        assertThatThrownBy(() -> jwtDecoder.decode(tokenType, token))
                .isInstanceOf(ExpiredTokenException.class)
                .hasMessage("토큰이 만료되었습니다.");
    }

    private static Stream<Arguments> encodeTestWithTokenTypeAndInvalidToken() {
        return Stream.of(
                Arguments.of(TokenType.ACCESS, null),
                Arguments.of(TokenType.ACCESS, ""),
                Arguments.of(TokenType.REFRESH, null),
                Arguments.of(TokenType.REFRESH, "")
        );
    }

    @ParameterizedTest(name = "TokenType이 {0}이고 토큰이 {1}일 때 토큰 디코딩을 할 수 없다")
    @MethodSource("encodeTestWithTokenTypeAndInvalidToken")
    void 비어_있는_토큰은_디코딩_할_수_없다(TokenType tokenType, String invalidToken) {
        // when & then
        assertThatThrownBy(() -> jwtDecoder.decode(tokenType, invalidToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("토큰이 존재하지 않거나 길이가 부족합니다.");
    }

    @ParameterizedTest
    @EnumSource(value = TokenType.class)
    void 유효한_토큰을_디코딩_한다(TokenType tokenType) {
        // given
        LocalDateTime publishTime = LocalDateTime.now(clock);
        String token = jwtEncoder.encode(publishTime, tokenType, 1L);

        // when
        PrivateClaims actual = jwtDecoder.decode(tokenType, token);

        // then
        assertAll(
                () -> assertThat(actual.userId()).isEqualTo(1L),
                () -> assertThat(actual.issuedAt()).isEqualTo(publishTime.truncatedTo(ChronoUnit.SECONDS))
        );
    }

    @ParameterizedTest
    @EnumSource(value = TokenType.class)
    void 토큰_발급자가_다른_토큰은_디코딩_할_수_없다(TokenType tokenType) throws KeyLengthException {
        // given
        TokenProperties otherIssuerTokenProperties = new TokenProperties(
                "thisIsA32ByteAccessTokenKeyForHS",
                "thisIsA32ByteRefreshTokenKeyForH",
                "thisIsA32ByteEncryptionKeyForAES",
                "other-issuer",
                43_200,
                259_200,
                43_200_000L,
                259_200_000L
        );
        byte[] encryptionKeyBytes = otherIssuerTokenProperties.encryptionKey()
                                                              .getBytes(StandardCharsets.UTF_8);
        SecretKey encryptionSecretKey = new SecretKeySpec(encryptionKeyBytes, "AES");
        JWEEncrypter otherServiceJweEncrypter = new AESEncrypter(encryptionSecretKey);
        JwtEncoder otherServiceJwtEncoder = new JwtEncoder(
                otherServiceJweEncrypter,
                jwsSignerFinder,
                otherIssuerTokenProperties,
                clock
        );
        String token = otherServiceJwtEncoder.encode(LocalDateTime.now(clock), tokenType, 1L);

        // when & then
        assertThatThrownBy(() -> jwtDecoder.decode(tokenType, token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("서비스에서 발급한 토큰이 아닙니다.");
    }

    @ParameterizedTest
    @EnumSource(value = TokenType.class)
    void 발급_시간이_없는_토큰은_디코딩_할_수_없다(TokenType tokenType) throws JOSEException {
        // given
        LocalDateTime now = LocalDateTime.now(clock);
        Date expirationTime = Date.from(now.plusMinutes(10)
                                           .atZone(clock.getZone())
                                           .toInstant());

        JWTClaimsSet claimsWithoutIssueTime = new JWTClaimsSet.Builder()
                .issuer(tokenProperties.issuer())
                .expirationTime(expirationTime)
                .claim("id", 1L)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsWithoutIssueTime);
        signedJWT.sign(jwsSignerFinder.findByTokenType(tokenType));

        JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.A256KW, EncryptionMethod.A256GCM)
                .contentType("JWT")
                .build();
        JWEObject jweObject = new JWEObject(jweHeader, new Payload(signedJWT));

        jweObject.encrypt(jweEncrypter);

        String token = jweObject.serialize();

        // when & then
        assertThatThrownBy(() -> jwtDecoder.decode(tokenType, token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("토큰 발급 시간이 존재하지 않습니다.");
    }

    @ParameterizedTest
    @EnumSource(value = TokenType.class)
    void 만료_시간이_없는_토큰은_디코딩_할_수_없다(TokenType tokenType) throws JOSEException {
        // given
        LocalDateTime now = LocalDateTime.now(clock);
        Date issueTime = Date.from(now.atZone(clock.getZone()).toInstant());

        JWTClaimsSet claimsWithoutExpiration = new JWTClaimsSet.Builder()
                .issuer(tokenProperties.issuer())
                .issueTime(issueTime)
                .claim("id", 1L)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsWithoutExpiration);
        signedJWT.sign(jwsSignerFinder.findByTokenType(tokenType));

        JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.A256KW, EncryptionMethod.A256GCM)
                .contentType("JWT")
                .build();
        JWEObject jweObject = new JWEObject(jweHeader, new Payload(signedJWT));

        jweObject.encrypt(jweEncrypter);

        String token = jweObject.serialize();

        // when & then
        assertThatThrownBy(() -> jwtDecoder.decode(tokenType, token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("토큰 만료 시간이 존재하지 않습니다.");
    }

    @ParameterizedTest
    @EnumSource(value = TokenType.class)
    void 사용자_ID가_없는_토큰은_디코딩_할_수_없다(TokenType tokenType) throws JOSEException {
        // given
        LocalDateTime now = LocalDateTime.now(clock);
        Date issueTime = Date.from(now.atZone(clock.getZone()).toInstant());
        Date expirationTime = Date.from(now.plusMinutes(10)
                                           .atZone(clock.getZone())
                                           .toInstant());

        JWTClaimsSet claimsWithoutUserId = new JWTClaimsSet.Builder()
                .issuer(tokenProperties.issuer())
                .issueTime(issueTime)
                .expirationTime(expirationTime)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsWithoutUserId);
        signedJWT.sign(jwsSignerFinder.findByTokenType(tokenType));

        JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.A256KW, EncryptionMethod.A256GCM)
                .contentType("JWT")
                .build();
        JWEObject jweObject = new JWEObject(jweHeader, new Payload(signedJWT));

        jweObject.encrypt(jweEncrypter);

        String token = jweObject.serialize();

        // when & then
        assertThatThrownBy(() -> jwtDecoder.decode(tokenType, token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("사용자 ID가 존재하지 않습니다.");
    }

    @ParameterizedTest
    @EnumSource(value = TokenType.class)
    void JWE_복호화는_성공했으나_내부_페이로드가_signed_JWT가_아닌_경우_디코딩_할_수_없다(TokenType tokenType) throws JOSEException {
        // given
        JWEHeader header = new JWEHeader(JWEAlgorithm.A256KW, EncryptionMethod.A256GCM);
        Payload notSignedJwtPayload = new Payload("{\"message\": \"이건 signed JWT 토큰이 아닙니다.\"}");
        JWEObject jweObject = new JWEObject(header, notSignedJwtPayload);

        jweObject.encrypt(jweEncrypter);

        String invalidContentToken = jweObject.serialize();

        // when & then
        assertThatThrownBy(() -> jwtDecoder.decode(tokenType, invalidContentToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("유효한 JWT 형식이 아닙니다.");
    }
}
