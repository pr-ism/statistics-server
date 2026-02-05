package com.prism.statistics.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.user.dto.request.ChangeNicknameRequest;
import com.prism.statistics.application.user.dto.response.ChangedNicknameResponse;
import com.prism.statistics.application.user.dto.response.UserInfoResponse;
import com.prism.statistics.application.user.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Sql("/sql/user/insert_user.sql")
    @Test
    void 회원_정보를_조회한다() {
        // given
        Long userId = 1L;

        // when
        UserInfoResponse actual = userService.findUserInfo(userId);

        // then
        assertThat(actual.nickname()).isEqualTo("기존닉네임");
    }

    @Test
    void 존재하지_않는_회원의_정보를_조회할_수_없다() {
        // given
        Long invalidUserId = 999L;

        // when & then
        assertThatThrownBy(() -> userService.findUserInfo(invalidUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("존재하지 않는 회원");
    }

    @Sql("/sql/user/insert_user.sql")
    @Test
    void 닉네임을_변경한다() {
        // given
        Long userId = 1L;
        ChangeNicknameRequest request = new ChangeNicknameRequest("변경된닉네임");

        // when
        ChangedNicknameResponse actual = userService.changedNickname(userId, request);

        // then
        assertThat(actual.changedNickname()).isEqualTo("변경된닉네임");
    }

    @Test
    void 존재하지_않는_회원의_닉네임을_변경할_수_없다() {
        // given
        Long invalidUserId = 999L;
        ChangeNicknameRequest request = new ChangeNicknameRequest("변경된닉네임");

        // when & then
        assertThatThrownBy(() -> userService.changedNickname(invalidUserId, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("존재하지 않는 회원");
    }
}
