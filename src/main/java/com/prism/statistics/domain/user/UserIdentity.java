package com.prism.statistics.domain.user;

import com.prism.statistics.domain.common.BaseEntity;
import com.prism.statistics.domain.user.vo.Social;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_identities")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserIdentity extends BaseEntity {

    private Long userId;

    @Embedded
    private Social social;

    public static UserIdentity create(Long userId, Social social) {
        return new UserIdentity(userId, social);
    }

    private UserIdentity(Long userId, Social social) {
        this.userId = userId;
        this.social = social;
    }
}
