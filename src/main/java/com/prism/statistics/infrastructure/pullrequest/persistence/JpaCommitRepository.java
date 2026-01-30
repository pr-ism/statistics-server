package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.Commit;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaCommitRepository extends ListCrudRepository<Commit, Long> {
}
