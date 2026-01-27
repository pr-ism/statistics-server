package com.prism.statistics.infrastructure.pullrequest.persistence;

import com.prism.statistics.domain.pullrequest.PrFile;
import com.prism.statistics.domain.pullrequest.repository.PrFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PrFileRepositoryAdapter implements PrFileRepository {

    private final JpaPrFileRepository jpaPrFileRepository;

    @Override
    public PrFile save(PrFile prFile) {
        return jpaPrFileRepository.save(prFile);
    }

    @Override
    public List<PrFile> saveAll(List<PrFile> prFiles) {
        return jpaPrFileRepository.saveAll(prFiles);
    }
}
