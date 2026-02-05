package com.prism.statistics.infrastructure.user.persistence;

import com.prism.statistics.domain.user.User;
import com.prism.statistics.domain.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    @Transactional
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id);
    }
}
