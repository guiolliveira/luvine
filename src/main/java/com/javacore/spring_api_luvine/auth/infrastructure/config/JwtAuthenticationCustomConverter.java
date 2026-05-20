package com.javacore.spring_api_luvine.auth.infrastructure.config;

import com.javacore.spring_api_luvine.auth.domain.exception.InvalidAuthenticationTokenException;
import com.javacore.spring_api_luvine.user.application.dto.CurrentUser;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationCustomConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public @Nullable AbstractAuthenticationToken convert(Jwt jwt) {
        String publicIdStr = jwt.getClaimAsString("publicId");

        if (publicIdStr == null) {
            throw new InvalidAuthenticationTokenException();
        }

        UUID publicId = UUID.fromString(publicIdStr);
        CurrentUser user = new CurrentUser(publicId);
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> authorities = jwt.getClaimAsStringList("authorities");

        if (authorities == null) return Collections.emptyList();

        return authorities
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}