package com.prism.statistics.application.user.dto.response;

import com.prism.statistics.domain.user.User;

public record UserInfoResponse(String nickname) {

    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(user.getNickname().getNicknameValue());
    }
}
