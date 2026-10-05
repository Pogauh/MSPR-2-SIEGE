package com.backendSiege.api.security;

import com.backendSiege.domain.models.User;
import com.backendSiege.domain.services.AuthService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Fournit à Spring Security les utilisateurs du siège. L'authentification passe par les jetons JWT, mais déclarer ce service
 * évite que Spring Boot crée un utilisateur en mémoire au mot de passe généré.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthService authService;

    public CustomUserDetailsService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = authService.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.email())
                .password(user.password())
                .authorities(user.roles().stream().map(role -> new SimpleGrantedAuthority(role.name())).toList())
                .build();
    }
}
