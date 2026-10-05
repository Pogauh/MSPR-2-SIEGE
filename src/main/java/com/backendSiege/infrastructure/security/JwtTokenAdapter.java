package com.backendSiege.infrastructure.security;

import com.backendSiege.domain.models.Role;
import com.backendSiege.domain.models.User;
import com.backendSiege.domain.ports.TokenPort;
import com.backendSiege.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/** Jetons JWT signés avec la clé {@code app.security.jwt.secret}. */
@Component
public class JwtTokenAdapter implements TokenPort {

    private final SecretKey signingKey;
    private final JwtProperties properties;

    public JwtTokenAdapter(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generate(User user) {
        Instant maintenant = Instant.now();
        return Jwts.builder()
                .subject(user.email())
                .claim("roles", user.roles().stream().map(Role::name).toList())
                .issuedAt(Date.from(maintenant))
                .expiration(Date.from(maintenant.plus(properties.expiration())))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String extractEmail(String token) {
        return claims(token).getSubject();
    }

    @Override
    public boolean isValid(String token, User user) {
        Claims claims = claims(token);
        return user.email().equals(claims.getSubject()) && !claims.getExpiration().before(new Date());
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}
