package com.prism.statistics.infrastructure.auth.persistence.exception;

public class OrphanedUserIdentityException extends RuntimeException {

    public OrphanedUserIdentityException() {
        super("회원의 소셜 정보가 고아 상태입니다.");
    }
}
