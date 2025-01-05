package ru.pyatkinmv.pognaleey.security;

import jakarta.annotation.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final String principal;

    public JwtAuthenticationToken(String principal) {
        super(null);
        this.principal = principal;
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
