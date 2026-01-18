package com.prism.statistics.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.auth.dto.LoggedInUserDto;
import com.prism.statistics.application.auth.exception.UserMissingException;
import com.prism.statistics.application.auth.exception.WithdrawnUserLoginException;
import com.prism.statistics.domain.user.enums.RegistrationId;
import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.infrastructure.auth.persistence.JpaUserIdentityRepository;
import com.prism.statistics.infrastructure.user.persistence.JpaUserRepository;
import com.prism.statistics.infrastructure.auth.persistence.UserSocialRepositoryAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SocialLoginServiceTest {

    @Autowired
    private SocialLoginService socialLoginService;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaUserIdentityRepository jpaUserIdentityRepository;

    @Autowired
    private UserSocialRepositoryAdapter userSocialRepositoryAdapter;

    @Test
    void 새로운_회원이_소셜_로그인으로_가입한다() {
        // when
        LoggedInUserDto actual = socialLoginService.login("KAKAO", "social-1");

        // then
        assertThat(actual.isSignUp()).isTrue();
    }

    @Sql("/sql/auth/insert_existing_user.sql")
    @Test
    void 기존_회원이_소셜_로그인_수행_시_가입하지_않고_로그인된다() {
        // when
        LoggedInUserDto actual = socialLoginService.login("KAKAO", "social-existing");

        // then
        assertAll(
                () -> assertThat(actual.isSignUp()).isFalse(),
                () -> assertThat(actual.id()).isEqualTo(1L)
        );
    }

    @Sql("/sql/auth/insert_withdrawn_user.sql")
    @Test
    void 탈퇴한_회원은_소셜_로그인을_할_수_없다() {
        // when & then
        assertThatThrownBy(() -> socialLoginService.login("KAKAO", "social-withdrawn"))
                .isInstanceOf(WithdrawnUserLoginException.class)
                .hasMessage("탈퇴한 회원입니다.");
    }

    @Sql("/sql/auth/insert_orphan_identity.sql")
    @Test
    void 회원의_소셜_정보면_존재하고_회원_정보가_존재하지_않는다면_로그인을_할_수_없다() {
        // when & then
        assertThatThrownBy(() -> socialLoginService.login("KAKAO", "orphan-social"))
                .isInstanceOf(UserMissingException.class)
                .hasMessage("회원의 소셜 정보는 존재하나 회원 정보는 존재하지 않습니다.");
    }

    @Test
    void 동일_소셜_정보로_동시에_로그인해도_단일_회원만_생성된다() throws Exception {
        // given
        String registrationId = RegistrationId.KAKAO.name();
        String socialId = "social-concurrent";
        int requestCount = 10;

        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(requestCount);
        List<Future<LoggedInUserDto>> futures = new ArrayList<>();

        try (ExecutorService executorService = Executors.newFixedThreadPool(requestCount)) {

            for (int i = 0; i < requestCount; i++) {
                futures.add(executorService.submit(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("로그인 시작 대기 중 인터럽트 발생", e);
                    }
                    try {
                        // when
                        return socialLoginService.login(registrationId, socialId);
                    } finally {
                        doneLatch.countDown();
                    }
                }));
            }

            readyLatch.await();
            startLatch.countDown();
            assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();
        }

        // then
        List<LoggedInUserDto> results = new ArrayList<>(requestCount);
        for (Future<LoggedInUserDto> future : futures) {
            results.add(future.get(5, TimeUnit.SECONDS));
        }

        Long createdUserId = results.get(0).id();

        assertAll(
                () -> assertThat(results).hasSize(requestCount),
                () -> assertThat(results).extracting(dto -> dto.id())
                                         .containsOnly(createdUserId),
                () -> assertThat(results).filteredOn(dto -> dto.isSignUp())
                                         .hasSize(1),
                () -> assertThat(jpaUserRepository.count()).isEqualTo(1),
                () -> assertThat(jpaUserIdentityRepository.count()).isEqualTo(1)
        );
    }
}
