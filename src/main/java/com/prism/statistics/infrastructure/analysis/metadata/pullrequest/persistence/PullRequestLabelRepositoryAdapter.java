package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequestLabel.pullRequestLabel;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestLabel;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelRepository;
import com.prism.statistics.infrastructure.common.MysqlDuplicateKeyDetector;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.exception.PullRequestLabelNotFoundException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PullRequestLabelRepositoryAdapter implements PullRequestLabelRepository {

    private final PullRequestLabelCreator pullRequestLabelCreator;
    private final JpaPullRequestLabelRepository jpaPullRequestLabelRepository;
    private final JPAQueryFactory queryFactory;
    private final MysqlDuplicateKeyDetector duplicateKeyDetector;

    @Override
    public PullRequestLabel saveOrFind(PullRequestLabel label) {
        try {
            return pullRequestLabelCreator.saveNew(label);
        } catch (DataIntegrityViolationException ex) {
            if (duplicateKeyDetector.isDuplicateKey(ex)) {
                return findByGithubPullRequestIdAndLabelName(
                        label.getGithubPullRequestId(), label.getLabelName()
                ).orElseThrow(() -> new PullRequestLabelNotFoundException());
            }
            throw ex;
        }
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

    @Override
    @Transactional
    public long deleteLabelByGithubId(Long githubPullRequestId, String labelName) {
        return queryFactory
                .delete(pullRequestLabel)
                .where(
                        pullRequestLabel.githubPullRequestId.eq(githubPullRequestId),
                        pullRequestLabel.labelName.eq(labelName)
                )
                .execute();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PullRequestLabel> findByGithubPullRequestIdAndLabelName(
            Long githubPullRequestId, String labelName
    ) {
        return jpaPullRequestLabelRepository.findByGithubPullRequestIdAndLabelName(
                githubPullRequestId, labelName
        );
    }
}
