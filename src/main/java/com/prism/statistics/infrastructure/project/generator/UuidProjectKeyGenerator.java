package com.prism.statistics.infrastructure.project.generator;

import com.prism.statistics.domain.project.ProjectApiKeyGenerator;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidProjectKeyGenerator implements ProjectApiKeyGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
