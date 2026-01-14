package com.prism.statistics.domain.auth;

import java.time.LocalDateTime;

public record PrivateClaims(Long userId, LocalDateTime issuedAt) {
}
