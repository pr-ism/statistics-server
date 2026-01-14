package com.prism.statistics.global.exception.dto.response;

import lombok.Getter;

@Getter
public enum DefaultErrorCode implements ErrorCode {

    D00("서버 에러");

    private final String message;

    DefaultErrorCode(String message) {
        this.message = message;
    }
    
    @Override
    public String getErrorCode() {
        return this.name();
    }
}
