package com.prism.statistics.domain.auth;

public enum TokenType {
    ACCESS, REFRESH;

    public boolean isAccessToken() {
        return this == TokenType.ACCESS;
    }
}
