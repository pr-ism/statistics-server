package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.size.PullRequestSize;
import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface JpaPullRequestSizeRepository extends ListCrudRepository<PullRequestSize, Long> {

    Optional<PullRequestSize> findByPullRequestId(Long pullRequestId);

    boolean existsByPullRequestId(Long pullRequestId);

    List<PullRequestSize> findBySizeGrade(SizeGrade sizeGrade);

    List<PullRequestSize> findBySizeGradeIn(List<SizeGrade> sizeGrades);
}
