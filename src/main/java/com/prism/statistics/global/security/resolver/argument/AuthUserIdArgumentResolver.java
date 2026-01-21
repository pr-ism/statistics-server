package com.prism.statistics.global.security.resolver.argument;

import com.prism.statistics.global.auth.AuthUserId;
import com.prism.statistics.global.security.core.OAuth2UserDetails;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AuthUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AuthUserId.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof OAuth2UserDetails userDetails) {
            return new AuthUserId(userDetails.getId());
        }

        throw new AuthenticationCredentialsNotFoundException("인증 정보가 없습니다.");
    }
}
