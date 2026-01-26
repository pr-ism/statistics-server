package com.prism.statistics.context.security;

import com.prism.statistics.global.security.core.OAuth2AuthenticationToken;
import com.prism.statistics.global.security.core.OAuth2UserDetails;
import java.util.Set;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
public class WithOAuth2UserSecurityContextFactory {

    public SecurityContext createSecurityContext(WithOAuth2User annotation) {
        OAuth2UserDetails userDetails = new OAuth2UserDetails(
                annotation.userId(),
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        OAuth2AuthenticationToken authenticationToken = new OAuth2AuthenticationToken(
                userDetails,
                userDetails.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationToken);
        return context;
    }
}
