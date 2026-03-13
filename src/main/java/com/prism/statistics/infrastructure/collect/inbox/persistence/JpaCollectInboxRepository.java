package com.prism.statistics.infrastructure.collect.inbox.persistence;

import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaCollectInboxRepository extends ListCrudRepository<CollectInbox, Long> {
}
