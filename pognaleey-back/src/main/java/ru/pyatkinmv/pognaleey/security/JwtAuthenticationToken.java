package ru.pyatkinmv.pognaleey.security;

import jakarta.annotation.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import ru.pyatkinmv.pognaleey.model.User;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final User principal;

    public JwtAuthenticationToken(User principal) {
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
