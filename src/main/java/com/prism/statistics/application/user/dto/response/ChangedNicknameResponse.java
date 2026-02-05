package com.prism.statistics.application.user.dto.response;

import com.prism.statistics.domain.user.User;

public record ChangedNicknameResponse(String changedNickname) {

    public static ChangedNicknameResponse create(User user) {
        return new ChangedNicknameResponse(user.getNickname().getNicknameValue());
    }
}
