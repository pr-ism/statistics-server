package com.prism.statistics.infrastructure.auth.jwt.exception;

public class ExpiredTokenException extends IllegalArgumentException {

    public ExpiredTokenException() {
        super("토큰이 만료되었습니다.");
    }
}
