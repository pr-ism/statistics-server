package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import com.prism.statistics.domain.analysis.metadata.pullrequest.Commit;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaCommitRepository extends ListCrudRepository<Commit, Long> {
}
