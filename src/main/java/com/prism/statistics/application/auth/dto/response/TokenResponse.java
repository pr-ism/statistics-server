package com.prism.statistics.application.auth.dto.response;

public record TokenResponse(String accessToken, String refreshToken, String tokenScheme) {
}
