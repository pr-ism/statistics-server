package com.prism.statistics.infrastructure.user.persistence;

import com.prism.statistics.domain.user.User;
import org.springframework.data.repository.CrudRepository;

public interface JpaUserRepository extends CrudRepository<User, Long> {
}
