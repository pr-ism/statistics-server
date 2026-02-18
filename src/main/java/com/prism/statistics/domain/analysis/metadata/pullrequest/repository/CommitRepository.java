package com.prism.statistics.domain.analysis.metadata.pullrequest.repository;

import com.prism.statistics.domain.analysis.metadata.pullrequest.Commit;

import java.util.List;

public interface CommitRepository {

    Commit save(Commit commit);

    List<Commit> saveAll(List<Commit> commits);

    List<String> findAllCommitShasByPullRequestId(Long pullRequestId);
}
