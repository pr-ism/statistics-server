package com.prism.statistics.domain.pullrequest.repository;

import com.prism.statistics.domain.pullrequest.Commit;

import java.util.List;

public interface CommitRepository {

    Commit save(Commit commit);

    List<Commit> saveAll(List<Commit> commits);
}
