package com.backendSiege.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Règles d'accès de l'API : routes publiques (connexion, inscription, documentation), routes réservées à certains rôles,
 * toutes les autres exigent un utilisateur authentifié par jeton JWT.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()

                        .requestMatchers(HttpMethod.GET, "/countries/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/farms/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/warehouses/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/zones/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/readings/**").authenticated()


                        .requestMatchers(HttpMethod.GET, "/lots").authenticated()
                        .requestMatchers(HttpMethod.GET, "/lot/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/lot/total").authenticated()

                        .requestMatchers(HttpMethod.GET, "/alertes/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_WAREHOUSE_MANAGER", "ROLE_FARM_MANAGER", "ROLE_QUALITY", "ROLE_SUPPLY_CHAIN")

                        .requestMatchers(HttpMethod.GET, "/alerts/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_WAREHOUSE_MANAGER", "ROLE_FARM_MANAGER", "ROLE_QUALITY", "ROLE_SUPPLY_CHAIN")

                        .requestMatchers(HttpMethod.POST, "/lot")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_FARM_MANAGER")

                        .requestMatchers(HttpMethod.PUT, "/lot/*/status")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_WAREHOUSE_MANAGER", "ROLE_QUALITY")

                        .requestMatchers(HttpMethod.PUT, "/lot/*/zones")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_WAREHOUSE_MANAGER")

                        .requestMatchers(HttpMethod.POST, "/mouvements/stockout")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_WAREHOUSE_MANAGER", "ROLE_FARM_MANAGER")

                        .requestMatchers(HttpMethod.GET, "/reporting/stock")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPPLY_CHAIN", "ROLE_HQ")

                        .requestMatchers(HttpMethod.GET, "/reporting/quality")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_QUALITY", "ROLE_HQ")

                        .requestMatchers(HttpMethod.GET, "/reporting/global")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
