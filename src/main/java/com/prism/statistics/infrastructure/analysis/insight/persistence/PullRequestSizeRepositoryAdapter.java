package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.size.PullRequestSize;
import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.domain.analysis.insight.size.repository.PullRequestSizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PullRequestSizeRepositoryAdapter implements PullRequestSizeRepository {

    private final JpaPullRequestSizeRepository jpaPullRequestSizeRepository;

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
}
