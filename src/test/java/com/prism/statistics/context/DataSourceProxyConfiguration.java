package com.prism.statistics.context;

import javax.sql.DataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class DataSourceProxyConfiguration {

    @Bean
    public QueryCountInspector queryCountInspector() {
        return new QueryCountInspector();
    }

    @Bean
    public BeanPostProcessor dataSourceProxyBeanPostProcessor(QueryCountInspector queryCountInspector) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DataSource dataSource && !(bean instanceof net.ttddyy.dsproxy.support.ProxyDataSource)) {
                    return ProxyDataSourceBuilder.create(dataSource)
                            .name("query-count-datasource")
                            .listener(queryCountInspector)
                            .build();
                }
                return bean;
            }
        };
    }
}
