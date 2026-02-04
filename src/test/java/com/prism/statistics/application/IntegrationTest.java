package com.prism.statistics.application;

import com.prism.statistics.application.analysis.insight.PullRequestMetricsService;
import com.prism.statistics.context.CleanupExecutionListener;
import com.prism.statistics.context.TestDuplicateKeyConfiguration;
import com.prism.statistics.infrastructure.auth.persistence.UserSocialRegistrar;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@Import(TestDuplicateKeyConfiguration.class)
@MockitoSpyBean(types = {UserSocialRegistrar.class, PullRequestMetricsService.class})
@ActiveProfiles({"local", "social"})
@TestExecutionListeners(listeners = CleanupExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public @interface IntegrationTest {
}
