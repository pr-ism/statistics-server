package com.prism.statistics.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.statistics.application.auth.GenerateTokenService;
import com.prism.statistics.application.auth.SocialLoginService;
import com.prism.statistics.domain.auth.TokenDecoder;
import com.prism.statistics.global.config.properties.CookieProperties;
import com.prism.statistics.global.config.properties.CorsProperties;
import com.prism.statistics.global.config.properties.TokenProperties;
import com.prism.statistics.global.security.core.OAuth2UserDetailsService;
import com.prism.statistics.global.security.filter.OAuth2AuthenticationFilter;
import com.prism.statistics.global.security.handler.OAuth2AccessDeniedHandler;
import com.prism.statistics.global.security.handler.OAuth2AuthenticationEntryPoint;
import com.prism.statistics.global.security.handler.OAuth2AuthenticationFailureHandler;
import com.prism.statistics.global.security.handler.OAuth2AuthorizationRequestFailureHandler;
import com.prism.statistics.global.security.handler.OAuth2SuccessHandler;
import com.prism.statistics.global.security.resolver.RegistrationIdValidatingAuthorizationRequestResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Profile("security")
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CorsProperties.class)
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final CorsProperties corsProperties;
    private final TokenProperties tokenProperties;
    private final CookieProperties cookieProperties;
    private final TokenDecoder tokenDecoder;
    private final SocialLoginService socialLoginService;
    private final GenerateTokenService generateTokenService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationRequestRedirectFilter redirectFilter =
                new OAuth2AuthorizationRequestRedirectFilter(oAuth2AuthorizationRequestResolver());
        redirectFilter.setAuthenticationFailureHandler(oAuth2AuthorizationRequestFailureHandler());

        http.csrf(configurer -> configurer.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .httpBasic(configurer -> configurer.disable())
            .formLogin(configurer -> configurer.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                    .requestMatchers("/docs/index.html").permitAll()
                    .requestMatchers("/webhook/**").permitAll()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(handler -> handler
                    .authenticationEntryPoint(oAuth2AuthenticationEntryPoint())
                    .accessDeniedHandler(oAuth2AccessDeniedHandler())
            )
            .oauth2Login(oauth -> oauth
                    .authorizationEndpoint(endpoint -> endpoint
                            .authorizationRequestResolver(oAuth2AuthorizationRequestResolver())
                    )
                    .successHandler(oAuth2SuccessHandler())
                    .failureHandler(oAuth2AuthenticationFailureHandler())
            )
            .addFilterBefore(redirectFilter, OAuth2AuthorizationRequestRedirectFilter.class)
            .addFilterBefore(oAuth2AuthenticationFilter(), OAuth2LoginAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OAuth2AuthenticationEntryPoint oAuth2AuthenticationEntryPoint() {
        return new OAuth2AuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public OAuth2AccessDeniedHandler oAuth2AccessDeniedHandler() {
        return new OAuth2AccessDeniedHandler(objectMapper);
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(
                objectMapper,
                tokenProperties,
                cookieProperties,
                socialLoginService,
                generateTokenService,
                oAuth2AuthenticationFailureHandler()
        );
    }

    @Bean
    public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return new OAuth2AuthenticationFailureHandler(objectMapper);
    }

    @Bean
    public OAuth2AuthenticationFilter oAuth2AuthenticationFilter() {
        return new OAuth2AuthenticationFilter(oAuth2UserDetailsService());
    }

    @Bean
    public OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver() {
        return new RegistrationIdValidatingAuthorizationRequestResolver(clientRegistrationRepository);
    }

    @Bean
    public AuthenticationFailureHandler oAuth2AuthorizationRequestFailureHandler() {
        return new OAuth2AuthorizationRequestFailureHandler(objectMapper);
    }

    @Bean
    public OAuth2UserDetailsService oAuth2UserDetailsService() {
        return new OAuth2UserDetailsService(tokenDecoder);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (!corsProperties.hasWildcardOrigins()) {
            configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        }

        if (corsProperties.hasOriginPatterns()) {
            configuration.setAllowedOriginPatterns(corsProperties.allowedOriginPatterns());
        }

        configuration.setAllowedMethods(corsProperties.allowedMethods());
        configuration.setAllowedHeaders(corsProperties.allowedHeaders());

        if (corsProperties.hasExposedHeaders()) {
            configuration.setExposedHeaders(corsProperties.exposedHeaders());
        }

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(corsProperties.maxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
