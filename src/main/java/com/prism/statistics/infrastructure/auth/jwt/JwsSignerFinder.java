package com.prism.statistics.infrastructure.auth.jwt;

import com.nimbusds.jose.JWSSigner;
import com.prism.statistics.domain.auth.TokenType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwsSignerFinder {

    private final JWSSigner accessTokenSigner;
    private final JWSSigner refreshTokenSigner;

    public JWSSigner findByTokenType(TokenType tokenType) {
        if (tokenType.isAccessToken()) {
            return accessTokenSigner;
        }

        return refreshTokenSigner;
    }
}
