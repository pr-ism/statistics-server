package com.prism.statistics.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.analysis.metadata.batch-insert")
public record BatchInsertProperties(
        @DefaultValue("100") int chunkSize
) {
    public BatchInsertProperties {
        if (chunkSize < 1) {
            throw new IllegalArgumentException(
                    "배치 INSERT 청크 크기는 1 이상이어야 합니다."
            );
        }
    }
}
