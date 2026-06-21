package com.backendSiege.domain.service;

import com.backendSiege.api.dto.auth.AuthResponseDTO;
import com.backendSiege.api.dto.auth.LoginRequestDTO;
import com.backendSiege.api.dto.auth.RegisterRequestDTO;
import com.backendSiege.domain.model.Role;
import com.backendSiege.infrastructure.entity.UserEntity;
import com.backendSiege.infrastructure.persistence.JpaUserRepository;
import com.backendSiege.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JpaUserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDTO register(RegisterRequestDTO request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Role role = request.getRole() != null
                ? request.getRole()
                : Role.ROLE_HEADQUARTER;

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(java.util.Set.of(role));

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new AuthResponseDTO(token);
    }

    public AuthResponseDTO login(LoginRequestDTO request) {

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponseDTO(token);
    }
    public UserEntity getCurrentUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException("Utilisateur introuvable")
                );

    }
}