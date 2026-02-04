package com.prism.statistics.global.config;

import com.prism.statistics.global.config.properties.PullRequestOpenedDerivedMetricsAsyncProperties;
import java.util.concurrent.Executor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
@EnableConfigurationProperties(PullRequestOpenedDerivedMetricsAsyncProperties.class)
public class AsyncConfig {

    @Bean(name = "asyncTaskExecutor")
    public Executor asyncTaskExecutor(PullRequestOpenedDerivedMetricsAsyncProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(properties.corePoolSize());
        executor.setMaxPoolSize(properties.maxPoolSize());
        executor.setQueueCapacity(properties.queueCapacity());
        executor.setKeepAliveSeconds(properties.keepAliveSeconds());
        executor.setThreadNamePrefix(properties.threadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(properties.waitForTasksToCompleteOnShutdown());
        executor.setAwaitTerminationSeconds(properties.awaitTerminationSeconds());
        executor.initialize();

        return executor;
    }
}
