package com.prism.statistics.global.security.resolver;

import com.prism.statistics.domain.user.enums.RegistrationId;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.StringUtils;

public class RegistrationIdValidatingAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String AUTHORIZATION_BASE_URI = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public RegistrationIdValidatingAuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository
    ) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                AUTHORIZATION_BASE_URI
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        String registrationId = extractRegistrationId(request);

        if (!StringUtils.hasText(registrationId)) {
            return null;
        }

        validateRegistrationId(registrationId);
        return delegate.resolve(request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        validateRegistrationId(clientRegistrationId);
        return delegate.resolve(request, clientRegistrationId);
    }

    private void validateRegistrationId(String registrationId) {
        if (!StringUtils.hasText(registrationId) || RegistrationId.notContains(registrationId)) {
            throw new AuthenticationServiceException("지원하지 않는 소셜 로그인 방식입니다.");
        }
    }

    private String extractRegistrationId(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();

        if (StringUtils.hasText(contextPath) && requestUri.startsWith(contextPath)) {
            requestUri = requestUri.substring(contextPath.length());
        }

        if (!requestUri.startsWith(AUTHORIZATION_BASE_URI)) {
            return "";
        }

        int registrationIdStart = AUTHORIZATION_BASE_URI.length();

        if (registrationIdStart >= requestUri.length()) {
            return "";
        }

        String path = requestUri.substring(registrationIdStart);

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        int nextSlash = path.indexOf("/");

        if (nextSlash >= 0) {
            return path.substring(0, nextSlash);
        }

        return path;
    }
}
