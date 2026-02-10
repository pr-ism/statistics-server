package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestLabelSavedEvent;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestLabel;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PullRequestLabelCreator {

    private final JpaPullRequestLabelRepository jpaPullRequestLabelRepository;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PullRequestLabel saveNew(PullRequestLabel pullRequestLabel) {
        PullRequestLabel saved = jpaPullRequestLabelRepository.save(pullRequestLabel);
        entityManager.flush();
        eventPublisher.publishEvent(new PullRequestLabelSavedEvent(saved));
        return saved;
    }
}
