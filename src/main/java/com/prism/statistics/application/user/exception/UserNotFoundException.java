package com.prism.statistics.application.user.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("존재하지 않는 회원");
    }
}
