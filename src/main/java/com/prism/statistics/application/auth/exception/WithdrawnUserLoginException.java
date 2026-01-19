package com.prism.statistics.application.auth.exception;

import org.springframework.security.core.AuthenticationException;

public class WithdrawnUserLoginException extends AuthenticationException {

    public WithdrawnUserLoginException() {
        super("탈퇴한 회원입니다.");
    }
}
