package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.exception;

public class PullRequestLabelNotFoundException extends RuntimeException {

    public PullRequestLabelNotFoundException() {
        super("PullRequestLabel을 찾을 수 없습니다.");
    }
}
