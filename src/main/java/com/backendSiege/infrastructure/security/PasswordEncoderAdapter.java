package com.backendSiege.infrastructure.security;

import com.backendSiege.domain.ports.PasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Hachage des mots de passe par le {@link PasswordEncoder} de l'application (BCrypt). */
@Component
public class PasswordEncoderAdapter implements PasswordHasherPort {

    private final PasswordEncoder passwordEncoder;

    public PasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String motDePasse) {
        return passwordEncoder.encode(motDePasse);
    }

    @Override
    public boolean matches(String motDePasse, String motDePasseHache) {
        return passwordEncoder.matches(motDePasse, motDePasseHache);
    }
}
