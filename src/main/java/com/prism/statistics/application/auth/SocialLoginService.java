package com.prism.statistics.application.auth;

import com.prism.statistics.application.auth.dto.LoggedInUserDto;
import com.prism.statistics.application.auth.exception.WithdrawnUserLoginException;
import com.prism.statistics.domain.auth.repository.UserSocialRepository;
import com.prism.statistics.domain.user.NicknameGenerator;
import com.prism.statistics.domain.user.User;
import com.prism.statistics.domain.user.UserIdentity;
import com.prism.statistics.domain.user.enums.RegistrationId;
import com.prism.statistics.domain.user.vo.Nickname;
import com.prism.statistics.domain.user.vo.Social;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("social")
@RequiredArgsConstructor
public class SocialLoginService {

    private final NicknameGenerator nicknameGenerator;
    private final UserSocialRepository userSocialRepository;

    @Transactional
    public LoggedInUserDto login(String registrationIdName, String socialId) {
        RegistrationId registrationId = RegistrationId.findBy(registrationIdName);
        Social social = new Social(registrationId, socialId);

        return userSocialRepository.find(social)
                                   .map(UserIdentity::getUserId)
                                   .flatMap(userSocialRepository::findById)
                                   .map(this::toLoggedInUserDto)
                                   .orElseGet(() -> signUp(social));
    }

    private LoggedInUserDto toLoggedInUserDto(User user) {
        validateUserState(user);

        return new LoggedInUserDto(user.getId(), user.getNickname().getValue(), false);
    }

    private LoggedInUserDto signUp(Social social) {
        Nickname nickname = nicknameGenerator.generate(bound -> ThreadLocalRandom.current().nextInt(bound));
        User user = User.create(nickname);
        User savedUser = userSocialRepository.save(user);
        UserIdentity userIdentity = UserIdentity.create(savedUser.getId(), social);

        userSocialRepository.save(userIdentity);
        return new LoggedInUserDto(savedUser.getId(), savedUser.getNickname().getValue(), true);
    }

    private void validateUserState(User user) {
        if (user.withdrawn()) {
            throw new WithdrawnUserLoginException();
        }
    }
}
