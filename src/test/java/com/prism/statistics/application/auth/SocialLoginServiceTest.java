package com.prism.statistics.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prism.statistics.application.auth.dto.LoggedInUserDto;
import com.prism.statistics.application.auth.exception.WithdrawnUserLoginException;
import com.prism.statistics.application.auth.fake.FakeUserSocialRepository;
import com.prism.statistics.domain.auth.repository.UserSocialRepository;
import com.prism.statistics.domain.user.NicknameGenerator;
import com.prism.statistics.domain.user.User;
import com.prism.statistics.domain.user.UserIdentity;
import com.prism.statistics.domain.user.enums.RegistrationId;
import com.prism.statistics.domain.user.vo.Social;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SocialLoginServiceTest {

    private SocialLoginService socialLoginService;
    private UserSocialRepository userSocialRepository;

    @BeforeEach
    void setUp() {
        NicknameGenerator nicknameGenerator = NicknameGenerator.of(
                List.of("섬세한"),
                List.of("보라")
        );

        userSocialRepository = new FakeUserSocialRepository();
        socialLoginService = new SocialLoginService(nicknameGenerator, userSocialRepository);
    }

    @Test
    void 새로운_사용자가_소셜_로그인_수행_시_가입_후_정보가_저장된다() {
        // given
        String registrationId = RegistrationId.KAKAO.name();
        String socialId = "social-1";

        // when
        LoggedInUserDto actual = socialLoginService.login(registrationId, socialId);

        // then
        assertThat(actual.isSignUp()).isTrue();
    }

    @Test
    void 기존_사용자가_소셜_로그인_수행_시_가입하지_않고_로그인된다() {
        // given
        socialLoginService.login("KAKAO", "social-2");

        // when
        LoggedInUserDto actual = socialLoginService.login("KAKAO", "social-2");

        // then
        assertThat(actual.isSignUp()).isFalse();
    }

    @Test
    void 탈퇴한_사용자는_소셜_로그인을_할_수_없다() {
        // given
        socialLoginService.login("KAKAO", "social-3");
        UserIdentity userIdentity = userSocialRepository.find(new Social(RegistrationId.KAKAO, "social-3"))
                                                        .orElseThrow();
        User user = userSocialRepository.findById(userIdentity.getUserId())
                                        .orElseThrow();
        user.withdraw();

        // when & then
        assertThatThrownBy(() -> socialLoginService.login("KAKAO", "social-3"))
                .isInstanceOf(WithdrawnUserLoginException.class);
    }
}
