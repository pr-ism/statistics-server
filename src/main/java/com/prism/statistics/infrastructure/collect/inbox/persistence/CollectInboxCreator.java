package com.prism.statistics.infrastructure.collect.inbox.persistence;

import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CollectInboxCreator {

    private final EntityManager entityManager;
    private final JpaCollectInboxRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveNew(CollectInbox inbox) {
        CollectInboxJpaEntity entity = new CollectInboxJpaEntity();
        entity.apply(inbox);
        repository.save(entity);
        entityManager.flush();
    }
}
