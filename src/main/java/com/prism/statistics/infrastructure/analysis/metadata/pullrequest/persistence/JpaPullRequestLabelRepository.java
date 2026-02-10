package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestLabel;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface JpaPullRequestLabelRepository extends ListCrudRepository<PullRequestLabel, Long> {

    Optional<PullRequestLabel> findByGithubPullRequestIdAndLabelName(Long githubPullRequestId, String labelName);
}
