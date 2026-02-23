package com.prism.statistics.domain.analysis.metadata.pullrequest.exception;

public class HeadCommitNotFoundException extends RuntimeException {

    public HeadCommitNotFoundException() {
        super("headCommitSha에 해당하는 커밋이 commits 목록에 존재하지 않습니다.");
    }
}
