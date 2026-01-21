package com.prism.statistics.global.config;

import com.prism.statistics.global.security.resolver.argument.AuthUserIdArgumentResolver;
import com.prism.statistics.global.security.resolver.argument.GuestIdArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Profile("security")
@Configuration
@RequiredArgsConstructor
public class SecurityAppConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthUserIdArgumentResolver());
        resolvers.add(new GuestIdArgumentResolver());
    }
}
