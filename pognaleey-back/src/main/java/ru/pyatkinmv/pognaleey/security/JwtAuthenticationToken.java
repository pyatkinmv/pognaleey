package ru.pyatkinmv.pognaleey.security;

import jakarta.annotation.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import ru.pyatkinmv.pognaleey.dto.UserDto;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final UserDto principal;

    public JwtAuthenticationToken(Long userId, String username, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = new UserDto(userId, username);
        this.setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Nullable
    @Override
    public Object getCredentials() {
        return null;
    }

}
