package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequestLabel.pullRequestLabel;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestLabel;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PullRequestLabelRepositoryAdapter implements PullRequestLabelRepository {

    private final JpaPullRequestLabelRepository jpaPullRequestLabelRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public PullRequestLabel save(PullRequestLabel label) {
        return jpaPullRequestLabelRepository.save(label);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long pullRequestId, String labelName) {
        return queryFactory
                .selectOne()
                .from(pullRequestLabel)
                .where(
                        pullRequestLabel.pullRequestId.eq(pullRequestId),
                        pullRequestLabel.labelName.eq(labelName)
                )
                .fetchFirst() != null;
    }

    @Override
    @Transactional
    public long deleteLabel(Long pullRequestId, String labelName) {
        return queryFactory
                .delete(pullRequestLabel)
                .where(
                        pullRequestLabel.pullRequestId.eq(pullRequestId),
                        pullRequestLabel.labelName.eq(labelName)
                )
                .execute();
    }
}
