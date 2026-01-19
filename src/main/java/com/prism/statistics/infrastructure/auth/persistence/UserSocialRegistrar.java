package com.prism.statistics.infrastructure.auth.persistence;

import com.prism.statistics.domain.auth.repository.UserSocialLoginResultDto;
import com.prism.statistics.domain.user.User;
import com.prism.statistics.domain.user.UserIdentity;
import com.prism.statistics.domain.user.vo.Social;
import com.prism.statistics.infrastructure.user.persistence.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserSocialRegistrar {

    private final JpaUserRepository jpaUserRepository;
    private final JpaUserIdentityRepository jpaUserIdentityRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserSocialLoginResultDto register(User user, Social social) {
        User savedUser = jpaUserRepository.save(user);
        UserIdentity userIdentity = UserIdentity.create(savedUser.getId(), social);

        jpaUserIdentityRepository.save(userIdentity);
        return UserSocialLoginResultDto.created(savedUser);
    }
}
