package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.Commit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCommitRepository extends JpaRepository<Commit, Long> {
}
