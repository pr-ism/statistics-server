package com.prism.statistics.application.auth.dto;

import com.prism.statistics.domain.auth.repository.UserSocialLoginResultDto;

public record LoggedInUserDto(Long id, String nickname, boolean isSignUp) {

    public static LoggedInUserDto create(UserSocialLoginResultDto userSocialLoginResultDto) {
        return new LoggedInUserDto(
                userSocialLoginResultDto.user().getId(),
                userSocialLoginResultDto.user().getNickname().getNicknameValue(),
                userSocialLoginResultDto.isSignUp()
        );
    }
}
