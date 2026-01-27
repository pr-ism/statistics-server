package com.prism.statistics.infrastructure.project.persistence.exception;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String name) {
        super("프로젝트를 찾을 수 없습니다: " + name);
    }
}
