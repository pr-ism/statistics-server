package com.prism.statistics.domain.auth;

public interface TokenDecoder {

    PrivateClaims decode(TokenType tokenType, String token);
}
