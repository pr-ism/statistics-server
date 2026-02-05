package com.prism.statistics.application.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeNicknameRequest(

        @NotBlank(message = "변경할 닉네임은 비어 있을 수 없습니다.")
        String changedNickname
) {
}
