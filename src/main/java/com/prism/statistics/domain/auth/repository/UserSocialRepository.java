package com.prism.statistics.domain.auth.repository;

import com.prism.statistics.domain.user.User;
import com.prism.statistics.domain.user.UserIdentity;
import com.prism.statistics.domain.user.vo.Social;
import java.util.Optional;

public interface UserSocialRepository {

    UserSocialLoginResultDto saveOrFind(User user, Social social);

    Optional<UserIdentity> find(Social social);

    Optional<User> findById(Long userId);
}
