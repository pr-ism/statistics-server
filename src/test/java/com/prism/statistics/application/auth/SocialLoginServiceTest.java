package com.prism.statistics.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.auth.dto.LoggedInUserDto;
import com.prism.statistics.application.auth.exception.UserMissingException;
import com.prism.statistics.application.auth.exception.WithdrawnUserLoginException;
import com.prism.statistics.domain.auth.repository.UserSocialLoginResultDto;
import com.prism.statistics.domain.user.User;
import com.prism.statistics.domain.user.UserIdentity;
import com.prism.statistics.domain.user.enums.RegistrationId;
import com.prism.statistics.domain.user.vo.Nickname;
import com.prism.statistics.domain.user.vo.Social;
import com.prism.statistics.infrastructure.auth.persistence.JpaUserIdentityRepository;
import com.prism.statistics.infrastructure.auth.persistence.UserSocialRegistrar;
import com.prism.statistics.infrastructure.user.persistence.JpaUserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

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
    private UserSocialRegistrar userSocialRegistrar;

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
    void 소셜_로그인_과정에서_race_condition_발생_도중_탈퇴_회원으로_상태가_변경되면_로그인을_할_수_없다() throws Exception {
        // given
        CountDownLatch registerStarted = new CountDownLatch(1);
        CountDownLatch proceedRegister = new CountDownLatch(1);

        doAnswer(
                invocation -> {
                    registerStarted.countDown();

                    try {
                        proceedRegister.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("회원 등록 대기 중 인터럽트 발생", e);
                    }

                    throw new DuplicateKeyException("동일 소셜 정보가 이미 등록되었습니다.");
                }
        ).when(userSocialRegistrar).register(any(User.class), any(Social.class));

        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            // when
            Future<LoggedInUserDto> future = executorService.submit(() -> socialLoginService.login("KAKAO", "withdrawn-race"));

            assertThat(registerStarted.await(3, TimeUnit.SECONDS)).isTrue();

            User withdrawnUser = User.create(Nickname.create("탈퇴-회원"));
            withdrawnUser.withdraw();
            jpaUserRepository.save(withdrawnUser);
            Social social = new Social(RegistrationId.KAKAO, "withdrawn-race");
            jpaUserIdentityRepository.save(UserIdentity.create(withdrawnUser.getId(), social));

            proceedRegister.countDown();

            // then
            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                    .hasCauseInstanceOf(WithdrawnUserLoginException.class)
                    .hasRootCauseMessage("탈퇴한 회원입니다.");
        } finally {
            proceedRegister.countDown();
        }
    }

    @Test
    @Transactional
    void 동일_소셜_정보로_동시에_로그인해도_단일_회원만_생성된다() throws Exception {
        // given
        String registrationId = RegistrationId.KAKAO.name();
        String socialId = "social-concurrent";
        int requestCount = 10;
        AtomicBoolean firstRegisterAttempt = new AtomicBoolean(false);
        AtomicReference<User> savedUserRef = new AtomicReference<>();
        CountDownLatch registerFinished = new CountDownLatch(1);

        doAnswer(invocation -> {
            if (firstRegisterAttempt.compareAndSet(false, true)) {
                try {
                    UserSocialLoginResultDto result = (UserSocialLoginResultDto) invocation.callRealMethod();
                    savedUserRef.set(result.user());
                    return result;
                } finally {
                    registerFinished.countDown();
                }
            }
            if (!registerFinished.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("첫 등록 완료 대기 타임아웃");
            }
            User savedUser = savedUserRef.get();
            if (savedUser == null) {
                throw new IllegalStateException("첫 등록 결과가 없습니다.");
            }
            return UserSocialLoginResultDto.found(savedUser);
        }).when(userSocialRegistrar).register(any(User.class), any(Social.class));

        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
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
                    // when
                    return socialLoginService.login(registrationId, socialId);
                }));
            }

            readyLatch.await();
            startLatch.countDown();
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
                                         .hasSize(1)
        );
    }
}
