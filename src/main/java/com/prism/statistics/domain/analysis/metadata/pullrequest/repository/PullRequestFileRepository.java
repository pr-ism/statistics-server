package com.prism.statistics.domain.analysis.metadata.pullrequest.repository;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestFile;

import java.util.List;

public interface PullRequestFileRepository {

    PullRequestFile save(PullRequestFile pullRequestFile);

    List<PullRequestFile> saveAll(List<PullRequestFile> pullRequestFiles);
}
