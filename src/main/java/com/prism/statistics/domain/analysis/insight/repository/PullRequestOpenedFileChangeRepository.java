package com.prism.statistics.domain.analysis.insight.repository;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedFileChange;
import java.util.List;

public interface PullRequestOpenedFileChangeRepository {

    List<PullRequestOpenedFileChange> saveAll(Iterable<PullRequestOpenedFileChange> fileChanges);
}
