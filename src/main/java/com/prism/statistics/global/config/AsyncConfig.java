package com.prism.statistics.global.config;

import com.prism.statistics.global.config.properties.BackfillAsyncProperties;
import com.prism.statistics.global.config.properties.PullRequestOpenedDerivedMetricsAsyncProperties;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@EnableAsync
@Configuration
@EnableConfigurationProperties({
        PullRequestOpenedDerivedMetricsAsyncProperties.class,
        BackfillAsyncProperties.class
})
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("Async Exception in {} : {}", method.getName(), ex.getMessage(), ex);
    }

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

    @Bean(name = "backfillExecutor")
    public Executor backfillExecutor(BackfillAsyncProperties properties) {
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
