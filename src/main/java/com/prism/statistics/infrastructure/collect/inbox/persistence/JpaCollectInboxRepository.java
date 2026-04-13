package com.prism.statistics.infrastructure.collect.inbox.persistence;

import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaCollectInboxRepository extends ListCrudRepository<CollectInboxJpaEntity, Long> {

    default Optional<CollectInbox> findDomainById(Long id) {
        return findById(id).map(entity -> entity.toDomain());
    }
}
