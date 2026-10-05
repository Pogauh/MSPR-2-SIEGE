package com.backendSiege.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Paramètres des jetons JWT ({@code app.security.jwt}).
 *
 * @param secret     clé de signature (au moins 64 caractères pour HS512), à fournir par {@code JWT_SECRET}
 * @param expiration durée de validité d'un jeton
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(String secret, @DefaultValue("24h") Duration expiration) {
}
