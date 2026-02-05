package com.prism.statistics.domain.user.repository;

import com.prism.statistics.domain.user.User;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);
}
