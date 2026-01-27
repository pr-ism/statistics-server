package com.prism.statistics.domain.pullrequest.repository;

import com.prism.statistics.domain.pullrequest.PrFile;

import java.util.List;

public interface PrFileRepository {

    PrFile save(PrFile prFile);

    List<PrFile> saveAll(List<PrFile> prFiles);
}
