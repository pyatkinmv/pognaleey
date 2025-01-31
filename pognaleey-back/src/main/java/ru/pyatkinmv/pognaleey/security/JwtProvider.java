package ru.pyatkinmv.pognaleey.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    private static final long MILLIS_IN_MINUTE = 1000 * 60;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.token-validity-time-minutes}")
    private long tokenValidityTimeMinutes;

    private SecretKey signingKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username, Collection<? extends GrantedAuthority> roles) {
        var claims = Jwts.claims().setSubject(username.toLowerCase());
        claims.put("roles", roles.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        claims.put("id", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + MILLIS_IN_MINUTE * tokenValidityTimeMinutes))
                .signWith(signingKey(secret), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey(secret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        //noinspection unchecked
        return (List<String>) extractClaims(token).get("roles");
    }

    public Long extractId(String token) {
        return extractClaims(token).get("id", Long.class);
    }

    public boolean isTokenValid(@Nullable String token) {
        if (token == null) {
            return false;
        }
        try {
            return extractClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}