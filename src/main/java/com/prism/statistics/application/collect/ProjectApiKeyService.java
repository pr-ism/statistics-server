package com.prism.statistics.application.collect;

import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectApiKeyService {

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public Long resolveProjectId(String apiKey) {
        return projectRepository.findIdByApiKey(apiKey)
                .orElseThrow(() -> new InvalidApiKeyException());
    }

    @Transactional(readOnly = true)
    public void validateApiKey(String apiKey) {
        if (!projectRepository.existsByApiKey(apiKey)) {
            throw new InvalidApiKeyException();
        }
    }

}
