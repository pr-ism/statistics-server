package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.exception;

public class PullRequestNotFoundException extends RuntimeException {

    public PullRequestNotFoundException() {
        super("PullRequest를 찾을 수 없습니다.");
    }
}
