package com.prism.statistics.global.security.resolver.argument;

import com.prism.statistics.global.auth.GuestId;
import com.prism.statistics.global.security.core.OAuth2UserDetails;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class GuestIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return GuestId.class.isAssignableFrom(parameter.getParameterType());
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
            return new GuestId(userDetails.getId());
        }

        return new GuestId(null);
    }
}
