package com.prism.statistics.presentation.auth.exception;

public class RefreshTokenNotFoundException extends IllegalArgumentException {

    public RefreshTokenNotFoundException() {
        super("Cookie에서 refreshToken을 찾을 수 없습니다.");
    }
}
