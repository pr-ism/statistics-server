package com.prism.statistics.application.auth.fake;

import com.prism.statistics.domain.auth.repository.UserSocialRepository;
import com.prism.statistics.domain.user.User;
import com.prism.statistics.domain.user.UserIdentity;
import com.prism.statistics.domain.user.vo.Social;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class FakeUserSocialRepository implements UserSocialRepository {

    private final AtomicLong userIdGenerator = new AtomicLong(1L);
    private final Map<SocialKey, UserIdentity> identities = new ConcurrentHashMap<>();
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private Long lastSavedUserId;

    @Override
    public User save(User user) {
        Long existingId = findExistingUserId(user);
        Long newId = (existingId != null) ? existingId : userIdGenerator.getAndIncrement();
        users.putIfAbsent(newId, user);
        lastSavedUserId = newId;
        return user;
    }

    @Override
    public UserIdentity save(UserIdentity userIdentity) {
        if (userIdentity.getUserId() == null) {
            Long userId = (lastSavedUserId != null) ? lastSavedUserId : userIdGenerator.getAndIncrement();
            setUserId(userIdentity, userId);
        }
        identities.put(SocialKey.from(userIdentity.getSocial()), userIdentity);
        return userIdentity;
    }

    @Override
    public Optional<UserIdentity> find(Social social) {
        return Optional.ofNullable(identities.get(SocialKey.from(social)));
    }

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public void deleteAllIdentitiesByUserId(Long userId) {
        identities.entrySet()
                  .removeIf(entry -> entry.getValue()
                                          .getUserId()
                                          .equals(userId));
    }

    private Long findExistingUserId(User user) {
        return users.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() == user)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
    }

    private void setUserId(UserIdentity userIdentity, Long userId) {
        try {
            Field userIdField = UserIdentity.class.getDeclaredField("userId");
            userIdField.setAccessible(true);
            userIdField.set(userIdentity, userId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("테스트용 userId 설정 실패", e);
        }
    }

    private record SocialKey(String registrationId, String socialId) {

        static SocialKey from(Social social) {
            return new SocialKey(
                    social.getRegistrationId().name(),
                    social.getSocialId()
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SocialKey socialKey = (SocialKey) o;
            return Objects.equals(registrationId, socialKey.registrationId)
                    && Objects.equals(socialId, socialKey.socialId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(registrationId, socialId);
        }
    }
}
