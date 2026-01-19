package com.prism.statistics.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.domain.user.enums.UserState;
import com.prism.statistics.domain.user.exception.AlreadyWithdrawnUserException;
import com.prism.statistics.domain.user.vo.Nickname;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserTest {

    @Test
    void 회원을_초기화한다() {
        // given
        Nickname nickname = Nickname.create("테스터");

        // when
        User actual = User.create(nickname);

        // then
        assertAll(
                () -> assertThat(actual.getId()).isNull(),
                () -> assertThat(actual.getNickname().getNicknameValue()).isEqualTo("테스터"),
                () -> assertThat(actual.getState().isActive()).isTrue()
        );
    }

    @Test
    void 닉네임이_비어_있으면_회원을_초기화_할_수_없다() {
        // when & then
        assertThatThrownBy(() -> User.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 비어 있을 수 없습니다.");
    }

    @Test
    void 닉네임을_변경한다() {
        // given
        User user = User.create(Nickname.create("테스터"));

        // when
        user.changeNickname("새 닉네임");

        // then
        assertThat(user.getNickname().getNicknameValue()).isEqualTo("새 닉네임");
    }

    @Test
    void 사용자를_탈퇴처리한다() {
        // given
        User user = User.create(Nickname.create("테스터"));

        // when
        user.withdraw();

        // then
        assertThat(user.getState()).isEqualTo(UserState.WITHDRAWAL);
    }

    @Test
    void 탈퇴한_사용자는_다시_탈퇴할_수_없다() {
        // given
        User user = User.create(Nickname.create("테스터"));

        user.withdraw();

        // when & then
        assertThatThrownBy(() -> user.withdraw())
                .isInstanceOf(AlreadyWithdrawnUserException.class)
                .hasMessage("이미 탈퇴한 회원입니다.");
    }

    @Test
    void 탈퇴_여부를_확인한다() {
        // given
        User user = User.create(Nickname.create("테스터"));

        // when
        user.withdraw();

        // then
        assertThat(user.withdrawn()).isTrue();
    }
}
