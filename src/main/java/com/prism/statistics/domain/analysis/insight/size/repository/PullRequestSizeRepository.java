package com.prism.statistics.domain.analysis.insight.size.repository;

import com.prism.statistics.domain.analysis.insight.size.PullRequestSize;
import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;

import java.util.List;
import java.util.Optional;

public interface PullRequestSizeRepository {

    PullRequestSize save(PullRequestSize size);

    Optional<PullRequestSize> findByPullRequestId(Long pullRequestId);

    boolean existsByPullRequestId(Long pullRequestId);

    List<PullRequestSize> findBySizeGrade(SizeGrade sizeGrade);

    List<PullRequestSize> findBySizeGradeIn(List<SizeGrade> sizeGrades);

    List<PullRequestSize> findAllByProjectId(Long projectId);
}
