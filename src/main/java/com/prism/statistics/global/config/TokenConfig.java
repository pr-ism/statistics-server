package com.prism.statistics.global.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.AESEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.prism.statistics.domain.auth.TokenDecoder;
import com.prism.statistics.domain.auth.TokenEncoder;
import com.prism.statistics.global.config.properties.TokenProperties;
import com.prism.statistics.infrastructure.auth.jwt.JwsSignerFinder;
import com.prism.statistics.infrastructure.auth.jwt.JwsVerifierFinder;
import com.prism.statistics.infrastructure.auth.jwt.JwtDecoder;
import com.prism.statistics.infrastructure.auth.jwt.JwtEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TokenProperties.class)
public class TokenConfig {

    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final String AES = "AES";

    private final Clock clock;
    private final TokenProperties tokenProperties;

    @Bean
    public TokenEncoder tokenEncoder(JWEEncrypter jweEncrypter, JwsSignerFinder jwsSignerFinder) {
        return new JwtEncoder(jweEncrypter, jwsSignerFinder, tokenProperties);
    }

    @Bean
    public TokenDecoder tokenDecoder(JWEDecrypter jweDecrypter, JwsVerifierFinder jwsVerifierFinder) {
        return new JwtDecoder(clock, jweDecrypter, jwsVerifierFinder, tokenProperties);
    }

    @Bean
    public JwsVerifierFinder jwsVerifierFinder(
            SecretKey accessTokenSecretKey,
            SecretKey refreshTokenSecretKey
    ) throws JOSEException {
        JWSVerifier accessTokenJwsVerifier = new MACVerifier(accessTokenSecretKey);
        JWSVerifier refreshTokenJwsVerifier = new MACVerifier(refreshTokenSecretKey);

        return new JwsVerifierFinder(accessTokenJwsVerifier, refreshTokenJwsVerifier);
    }

    @Bean
    public JwsSignerFinder jwsSignerFinder(
            SecretKey accessTokenSecretKey,
            SecretKey refreshTokenSecretKey
    ) throws KeyLengthException {
        MACSigner accessTokenSigner = new MACSigner(accessTokenSecretKey);
        MACSigner refreshTokenSigner = new MACSigner(refreshTokenSecretKey);

        return new JwsSignerFinder(accessTokenSigner, refreshTokenSigner);
    }

    @Bean
    public SecretKey accessTokenSecretKey() {
        byte[] accessTokenKeyBytes = tokenProperties.accessKey().getBytes(StandardCharsets.UTF_8);

        return new SecretKeySpec(accessTokenKeyBytes, HMAC_SHA_256);
    }

    @Bean
    public SecretKey refreshTokenSecretKey() {
        byte[] refreshTokenKeyBytes = tokenProperties.refreshKey().getBytes(StandardCharsets.UTF_8);

        return new SecretKeySpec(refreshTokenKeyBytes, HMAC_SHA_256);
    }

    @Bean
    public JWEDecrypter jweDecrypter(SecretKey gcmAesSecretKey) throws KeyLengthException {
        return new AESDecrypter(gcmAesSecretKey);
    }

    @Bean
    public JWEEncrypter jweEncrypter(SecretKey gcmAesSecretKey) throws KeyLengthException {
        return new AESEncrypter(gcmAesSecretKey);
    }

    @Bean
    public SecretKeySpec gcmAesSecretKey() {
        return new SecretKeySpec(decodeKey(), 0, 24, AES);
    }

    private byte[] decodeKey() {
        String encryptionKey = tokenProperties.encryptionKey();

        try {
            return Base64.getDecoder().decode(encryptionKey);
        } catch (IllegalArgumentException e) {
            return encryptionKey.getBytes(StandardCharsets.UTF_8);
        }
    }
}
