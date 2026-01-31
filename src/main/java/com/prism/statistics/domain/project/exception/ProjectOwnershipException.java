package com.prism.statistics.domain.project.exception;

public class ProjectOwnershipException extends RuntimeException {

    public ProjectOwnershipException() {
        super("프로젝트를 찾을 수 없습니다.");
    }
}
