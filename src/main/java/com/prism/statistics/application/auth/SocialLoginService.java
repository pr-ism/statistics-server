package com.prism.statistics.application.auth;

import com.prism.statistics.application.auth.dto.LoggedInUserDto;
import com.prism.statistics.application.auth.exception.UserMissingException;
import com.prism.statistics.application.auth.exception.WithdrawnUserLoginException;
import com.prism.statistics.domain.auth.repository.UserSocialRepository;
import com.prism.statistics.domain.auth.repository.UserSocialLoginResultDto;
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

@Service
@Profile("social")
@RequiredArgsConstructor
public class SocialLoginService {

    private final NicknameGenerator nicknameGenerator;
    private final UserSocialRepository userSocialRepository;

    public LoggedInUserDto login(String registrationIdName, String socialId) {
        RegistrationId registrationId = RegistrationId.findBy(registrationIdName);
        Social social = new Social(registrationId, socialId);

        return userSocialRepository.find(social)
                                   .map(identity -> internalLogin(identity))
                                   .orElseGet(() -> signUp(social));
    }

    private LoggedInUserDto internalLogin(UserIdentity identity) {
        return userSocialRepository.findById(identity.getUserId())
                                   .map(user -> toLoggedInUserDto(user, identity.getUserId()))
                                   .orElseThrow(() -> new UserMissingException());
    }

    private LoggedInUserDto signUp(Social social) {
        Nickname nickname = nicknameGenerator.generate(bound -> ThreadLocalRandom.current().nextInt(bound));
        User user = User.create(nickname);
        UserSocialLoginResultDto userSocialLoginResultDto = userSocialRepository.saveOrFind(user, social);

        validateUserState(userSocialLoginResultDto.user());

        return LoggedInUserDto.create(userSocialLoginResultDto);
    }

    private LoggedInUserDto toLoggedInUserDto(User user, Long userId) {
        validateUserState(user);

        return new LoggedInUserDto(userId, user.getNickname().getNicknameValue(), false);
    }

    private void validateUserState(User user) {
        if (user.withdrawn()) {
            throw new WithdrawnUserLoginException();
        }
    }
}
