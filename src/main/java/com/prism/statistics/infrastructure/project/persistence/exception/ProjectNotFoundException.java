package com.prism.statistics.infrastructure.project.persistence.exception;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException() {
        super("유효하지 않은 API Key입니다.");
    }
}
