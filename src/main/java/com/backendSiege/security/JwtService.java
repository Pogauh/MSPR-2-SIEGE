package com.backendSiege.security;

import com.backendSiege.infrastructure.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "1234567890123456789012345678901234567890123456789012345678901234";

    public String generateToken(UserEntity user) {

        return Jwts.builder()

                .subject(user.getEmail())

                .claim("roles", user.getRoles().stream().map(Enum::name).toList())

                .issuedAt(new Date())

                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 1000 * 60 * 60 * 24
                        )
                )

                .signWith(getSignKey())

                .compact();
    }


    public String extractEmail(String token) {

        return extractClaims(token).getSubject();
    }


    public boolean isTokenValid(String token, UserEntity user) {

        String email = extractEmail(token);

        return email.equals(user.getEmail())
                && !isTokenExpired(token);
    }


    private boolean isTokenExpired(String token) {

        return extractClaims(token)

                .getExpiration()

                .before(new Date());
    }


    private Claims extractClaims(String token) {

        return Jwts.parser()

                .verifyWith(getSignKey())

                .build()

                .parseSignedClaims(token)

                .getPayload();
    }


    private SecretKey getSignKey() {

        byte[] keyBytes =

                Decoders.BASE64.decode(
                        java.util.Base64.getEncoder()
                                .encodeToString(
                                        SECRET_KEY.getBytes()
                                )
                );

        return Keys.hmacShaKeyFor(keyBytes);
    }

}