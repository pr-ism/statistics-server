package com.prism.statistics.infrastructure.auth.persistence;

import static com.prism.statistics.domain.user.QUser.user;
import static com.prism.statistics.domain.user.QUserIdentity.userIdentity;

import com.prism.statistics.domain.auth.repository.UserSocialRepository;
import com.prism.statistics.domain.auth.repository.UserSocialLoginResultDto;
import com.prism.statistics.domain.user.User;
import com.prism.statistics.domain.user.UserIdentity;
import com.prism.statistics.domain.user.vo.Social;
import com.prism.statistics.infrastructure.auth.persistence.exception.OrphanedUserIdentityException;
import com.prism.statistics.infrastructure.user.persistence.JpaUserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserSocialRepositoryAdapter implements UserSocialRepository {

    private final JPAQueryFactory queryFactory;
    private final JpaUserRepository jpaUserRepository;
    private final UserSocialRegistrar userSocialRegistrar;

    @Override
    public UserSocialLoginResultDto saveOrFind(User user, Social social) {
        try {
            return userSocialRegistrar.register(user, social);
        } catch (DataIntegrityViolationException e) {
            return findExistingUser(social).map(loginUser -> UserSocialLoginResultDto.found(loginUser))
                                           .orElseThrow(() -> new OrphanedUserIdentityException());
        }
    }

    private Optional<User> findExistingUser(Social social) {
        if (social == null) {
            return Optional.empty();
        }

        User result = queryFactory.select(user)
                                  .from(user)
                                  .join(userIdentity).on(user.id.eq(userIdentity.userId))
                                  .where(
                                          userIdentity.social.registrationId.eq(social.getRegistrationId()),
                                          userIdentity.social.socialId.eq(social.getSocialId())
                                  )
                                  .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<UserIdentity> find(Social social) {
        if (social == null) {
            return Optional.empty();
        }

        UserIdentity result = queryFactory.selectFrom(userIdentity)
                                         .where(
                                                 userIdentity.social.registrationId.eq(social.getRegistrationId()),
                                                 userIdentity.social.socialId.eq(social.getSocialId())
                                         )
                                         .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return jpaUserRepository.findById(userId);
    }
}
