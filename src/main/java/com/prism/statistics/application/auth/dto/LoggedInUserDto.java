package com.prism.statistics.application.auth.dto;

public record LoggedInUserDto(Long id, String nickname, boolean isSignUp) {
}
