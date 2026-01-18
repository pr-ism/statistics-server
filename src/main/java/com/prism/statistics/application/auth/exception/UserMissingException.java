package com.prism.statistics.application.auth.exception;

public class UserMissingException extends RuntimeException {

    public UserMissingException() {
        super("회원의 소셜 정보는 존재하나 회원 정보는 존재하지 않습니다.");
    }
}
