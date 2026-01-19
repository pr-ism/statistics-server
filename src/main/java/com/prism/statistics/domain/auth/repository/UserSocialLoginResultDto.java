package com.prism.statistics.domain.auth.repository;

import com.prism.statistics.domain.user.User;

public record UserSocialLoginResultDto(User user, boolean isSignUp) {

    public static UserSocialLoginResultDto created(User user) {
        return new UserSocialLoginResultDto(user, true);
    }

    public static UserSocialLoginResultDto found(User user) {
        return new UserSocialLoginResultDto(user, false);
    }
}
