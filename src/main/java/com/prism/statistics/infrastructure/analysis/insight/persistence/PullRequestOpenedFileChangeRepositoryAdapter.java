package com.prism.statistics.infrastructure.analysis.insight.persistence;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedFileChange;
import com.prism.statistics.domain.analysis.insight.repository.PullRequestOpenedFileChangeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PullRequestOpenedFileChangeRepositoryAdapter implements
        PullRequestOpenedFileChangeRepository {

    private final JpaPullRequestOpenedFileChangeRepository jpaPullRequestOpenedFileChangeRepository;

    @Override
    public List<PullRequestOpenedFileChange> saveAll(Iterable<PullRequestOpenedFileChange> fileChanges) {
        return jpaPullRequestOpenedFileChangeRepository.saveAll(fileChanges);
    }
}
