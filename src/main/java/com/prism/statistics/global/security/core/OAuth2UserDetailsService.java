package com.prism.statistics.global.security.core;

import com.prism.statistics.domain.auth.PrivateClaims;
import com.prism.statistics.domain.auth.TokenDecoder;
import com.prism.statistics.domain.auth.TokenType;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class OAuth2UserDetailsService implements UserDetailsService {

    private final TokenDecoder tokenDecoder;

    @Override
    public OAuth2UserDetails loadUserByUsername(String token) throws UsernameNotFoundException {
        PrivateClaims privateClaims = tokenDecoder.decode(TokenType.ACCESS, token);

        return convert(privateClaims);
    }

    private OAuth2UserDetails convert(PrivateClaims privateClaims) {
        return new OAuth2UserDetails(
                privateClaims.userId(),
                Set.of(new SimpleGrantedAuthority(String.valueOf(privateClaims.userId())))
        );
    }
}
