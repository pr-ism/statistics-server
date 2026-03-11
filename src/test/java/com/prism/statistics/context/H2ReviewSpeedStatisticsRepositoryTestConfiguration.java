package com.prism.statistics.context;

import com.prism.statistics.domain.statistics.repository.ReviewSpeedStatisticsRepository;
import com.prism.statistics.infrastructure.statistics.persistence.H2ReviewSpeedStatisticsRepositoryAdapter;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class H2ReviewSpeedStatisticsRepositoryTestConfiguration {

    @Bean
    @Primary
    public ReviewSpeedStatisticsRepository reviewSpeedStatisticsRepository(JPAQueryFactory queryFactory) {
        return new H2ReviewSpeedStatisticsRepositoryAdapter(queryFactory);
    }
}
