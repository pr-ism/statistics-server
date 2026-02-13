package com.prism.statistics.infrastructure.analysis.insight.persistence;

import static com.prism.statistics.domain.analysis.insight.size.QPullRequestSize.pullRequestSize;
import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequest.pullRequest;

import com.prism.statistics.domain.analysis.insight.size.PullRequestSize;
import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.domain.analysis.insight.size.repository.PullRequestSizeRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PullRequestSizeRepositoryAdapter implements PullRequestSizeRepository {

    private final JpaPullRequestSizeRepository jpaPullRequestSizeRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public PullRequestSize save(PullRequestSize size) {
        return jpaPullRequestSizeRepository.save(size);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PullRequestSize> findByPullRequestId(Long pullRequestId) {
        return jpaPullRequestSizeRepository.findByPullRequestId(pullRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPullRequestId(Long pullRequestId) {
        return jpaPullRequestSizeRepository.existsByPullRequestId(pullRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PullRequestSize> findBySizeGrade(SizeGrade sizeGrade) {
        return jpaPullRequestSizeRepository.findBySizeGrade(sizeGrade);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PullRequestSize> findBySizeGradeIn(List<SizeGrade> sizeGrades) {
        return jpaPullRequestSizeRepository.findBySizeGradeIn(sizeGrades);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PullRequestSize> findAllByProjectId(Long projectId) {
        return queryFactory
                .selectFrom(pullRequestSize)
                .join(pullRequest)
                .on(pullRequestSize.pullRequestId.eq(pullRequest.id))
                .where(pullRequest.projectId.eq(projectId))
                .fetch();
    }
}
