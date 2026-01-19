package com.prism.statistics.infrastructure.auth.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.domain.auth.repository.UserSocialLoginResultDto;
import com.prism.statistics.domain.user.User;
import com.prism.statistics.domain.user.enums.RegistrationId;
import com.prism.statistics.domain.user.vo.Nickname;
import com.prism.statistics.domain.user.vo.Social;
import com.prism.statistics.infrastructure.auth.persistence.exception.OrphanedUserIdentityException;
import com.prism.statistics.infrastructure.user.persistence.JpaUserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserSocialRepositoryAdapterTest {

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaUserIdentityRepository jpaUserIdentityRepository;

    @Autowired
    private UserSocialRepositoryAdapter userSocialRepositoryAdapter;

    @Autowired
    private UserSocialRegistrar userSocialRegistrar;

    @Sql("/sql/auth/insert_existing_user.sql")
    @Test
    void 중복_소셜_정보로_저장하면_기존_사용자를_반환한다() {
        // given
        Nickname nickname = Nickname.create("중복-사용자");
        User user = User.create(nickname);
        Social social = new Social(RegistrationId.KAKAO, "social-existing");

        doThrow(new DuplicateKeyException("중복된 소셜 정보"))
                .when(userSocialRegistrar)
                .register(any(User.class), any(Social.class));

        // when
        UserSocialLoginResultDto actual = userSocialRepositoryAdapter.saveOrFind(user, social);

        assertAll(
                () -> assertThat(actual.isSignUp()).isFalse(),
                () -> assertThat(actual.user().getId()).isEqualTo(1L),
                () -> assertThat(jpaUserRepository.count()).isEqualTo(1),
                () -> assertThat(jpaUserIdentityRepository.count()).isEqualTo(1)
        );
    }

    @Sql("/sql/auth/insert_orphan_identity.sql")
    @Test
    void 소셜_정보만_있고_회원_정보가_없다면_DB_상태가_정상적이지_않음을_알린다() {
        // given
        Nickname nickname = Nickname.create("고아-사용자");
        User user = User.create(nickname);
        Social social = new Social(RegistrationId.KAKAO, "orphan-social");

        doThrow(new DuplicateKeyException("중복된 소셜 정보"))
                .when(userSocialRegistrar)
                .register(any(User.class), any(Social.class));

        // when & then
        assertThatThrownBy(() -> userSocialRepositoryAdapter.saveOrFind(user, social))
                .isInstanceOf(OrphanedUserIdentityException.class)
                .hasMessage("회원의 소셜 정보가 고아 상태입니다.");
    }
}
