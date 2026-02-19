package com.prism.statistics.domain.analysis.metadata.pullrequest.repository;

import com.prism.statistics.domain.analysis.metadata.pullrequest.Commit;

import java.util.List;
import java.util.Set;

public interface CommitRepository {

    Commit save(Commit commit);

    List<Commit> saveAll(List<Commit> commits);

    Set<String> findAllCommitShasByPullRequestId(Long pullRequestId);
}
