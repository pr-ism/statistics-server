package com.prism.statistics.domain.analysis.metadata.pullrequest.exception;

public class PullRequestNotFoundException extends RuntimeException {

    public PullRequestNotFoundException() {
        super("PullRequest를 찾을 수 없습니다.");
    }
}
