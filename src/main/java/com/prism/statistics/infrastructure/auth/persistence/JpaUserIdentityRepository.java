package com.prism.statistics.infrastructure.auth.persistence;

import com.prism.statistics.domain.user.UserIdentity;
import org.springframework.data.repository.CrudRepository;

public interface JpaUserIdentityRepository extends CrudRepository<UserIdentity, Long> {
}
