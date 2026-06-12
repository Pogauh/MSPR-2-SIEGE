package com.backendSiege.api.controller;

import com.backendSiege.api.dto.AuthResponseDTO;
import com.backendSiege.api.dto.LoginRequestDTO;
import com.backendSiege.infrastructure.security.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            // --- CRÉATION DU COOKIE ---
            Cookie jwtCookie = new Cookie("token", jwt);
            jwtCookie.setHttpOnly(true);      // Empêche l'accès au token par JavaScript (protection XSS)
            jwtCookie.setPath("/");           // Accessible sur tout le domaine
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 heures
            // jwtCookie.setSecure(true);     // À décommenter si HTTPS

            response.addCookie(jwtCookie);    // Ajout du cookie à la réponse

            return ResponseEntity.ok("Authentification réussie, jeton stocké dans le cookie.");

        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(401).body("Erreur : Mot de passe incorrect.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponseDTO> logout(HttpServletResponse response) {
        // Pour déconnecter, on écrase le cookie avec un temps de vie nul
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(new AuthResponseDTO("Déconnexion réussie", null));
    }
}