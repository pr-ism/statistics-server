package com.prism.statistics.domain.user;

import com.prism.statistics.domain.common.BaseTimeEntity;
import com.prism.statistics.domain.user.enums.UserState;
import com.prism.statistics.domain.user.vo.Nickname;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Embedded
    private Nickname nickname;

    @Enumerated(EnumType.STRING)
    private UserState state;

    public static User create(Nickname nickname) {
        return new User(nickname);
    }

    private User(Nickname nickname) {
        this.nickname = nickname;
        this.state = UserState.ACTIVE;
    }

    public void changeNickname(String changedNickname) {
        this.nickname = this.nickname.changeNickname(changedNickname);
    }

    public void withdraw() {
        if (state.isWithdrawal()) {
            throw new IllegalStateException("이미 탈퇴한 사용자입니다.");
        }

        this.state = UserState.WITHDRAWAL;
    }

    public boolean withdrawn() {
        return state.isWithdrawal();
    }
}
