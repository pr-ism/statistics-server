package com.prism.statistics.infrastructure.auth.persistence;

import static com.prism.statistics.domain.user.QUserIdentity.userIdentity;

import com.prism.statistics.domain.auth.repository.UserSocialRepository;
import com.prism.statistics.domain.user.User;
import com.prism.statistics.domain.user.UserIdentity;
import com.prism.statistics.domain.user.vo.Social;
import com.prism.statistics.infrastructure.user.persistence.JpaUserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserSocialRepositoryAdapter implements UserSocialRepository {

    private final JPAQueryFactory queryFactory;
    private final JpaUserRepository jpaUserRepository;
    private final JpaUserIdentityRepository jpaUserIdentityRepository;

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public UserIdentity save(UserIdentity userIdentity) {
        return jpaUserIdentityRepository.save(userIdentity);
    }

    @Override
    public Optional<UserIdentity> find(Social social) {
        return Optional.ofNullable(
                queryFactory.selectFrom(userIdentity)
                        .where(
                                userIdentity.social.registrationId.eq(social.getRegistrationId()),
                                userIdentity.social.socialId.eq(social.getSocialId())
                        )
                        .fetchOne()
        );
    }

    @Override
    public Optional<User> findById(Long userId) {
        return jpaUserRepository.findById(userId);
    }

    @Override
    @Transactional
    public void deleteAllIdentitiesByUserId(Long userId) {
        queryFactory.delete(userIdentity)
                    .where(userIdentity.userId.eq(userId))
                    .execute();
    }
}
