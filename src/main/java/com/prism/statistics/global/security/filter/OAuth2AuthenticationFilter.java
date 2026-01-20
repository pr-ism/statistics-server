package com.prism.statistics.global.security.filter;

import com.prism.statistics.global.security.core.OAuth2AuthenticationToken;
import com.prism.statistics.global.security.core.OAuth2UserDetails;
import com.prism.statistics.global.security.core.OAuth2UserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class OAuth2AuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_SCHEME = "Bearer ";

    private final OAuth2UserDetailsService oAuth2UserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authorization)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = parseBearerToken(authorization);

        try {
            OAuth2UserDetails userDetails = oAuth2UserDetailsService.loadUserByUsername(token);

            setAuthentication(userDetails);
        } catch (RuntimeException ex) {
            throw new AuthenticationServiceException("토큰 검증에 실패했습니다.", ex);
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(OAuth2UserDetails oAuth2UserDetails) {
        SecurityContextHolder.getContext()
                             .setAuthentication(
                                     new OAuth2AuthenticationToken(
                                             oAuth2UserDetails,
                                             oAuth2UserDetails.getAuthorities()
                                     )
                             );
    }

    private String parseBearerToken(String authorization) {
        if (!authorization.startsWith(TOKEN_SCHEME)) {
            throw new AuthenticationServiceException("Authorization 헤더가 Bearer 타입이 아닙니다.");
        }

        String token = authorization.substring(TOKEN_SCHEME.length());

        if (!StringUtils.hasText(token)) {
            throw new AuthenticationServiceException("Bearer 토큰이 비어 있습니다.");
        }

        return token;
    }
}
