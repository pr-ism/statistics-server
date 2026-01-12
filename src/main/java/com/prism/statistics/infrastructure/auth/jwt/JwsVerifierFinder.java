package com.prism.statistics.infrastructure.auth.jwt;

import com.nimbusds.jose.JWSVerifier;
import com.prism.statistics.domain.auth.TokenType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwsVerifierFinder {

    private final JWSVerifier accessTokenJwsVerifier;
    private final JWSVerifier refreshTokenJwsVerifier;

    public JWSVerifier findByTokenType(TokenType tokenType) {
        if (tokenType.isAccessToken()) {
            return accessTokenJwsVerifier;
        }

        return refreshTokenJwsVerifier;
    }
}
