package com.prism.statistics.infrastructure.pullrequest.persistence.exception;

public class PullRequestNotFoundException extends RuntimeException {

    public PullRequestNotFoundException() {
        super("해당 PR을 찾을 수 없습니다.");
    }
}
