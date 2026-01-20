package com.prism.statistics.global.security.core;

import java.util.Collection;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class OAuth2UserDetails implements UserDetails {

    private static final String DEFAULT_IGNORE_VALUE = "";

    private final Long id;
    private final Set<GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return DEFAULT_IGNORE_VALUE;
    }

    @Override
    public String getUsername() {
        return String.valueOf(id);
    }
}
