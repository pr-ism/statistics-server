package com.prism.statistics.domain.user.exception;

public class AlreadyWithdrawnUserException extends IllegalStateException {

    public AlreadyWithdrawnUserException() {
        super("이미 탈퇴한 회원입니다.");
    }
}
