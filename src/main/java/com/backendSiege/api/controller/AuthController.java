package com.backendSiege.api.controller;

import com.backendSiege.api.dto.auth.AuthResponseDTO;
import com.backendSiege.api.dto.auth.LoginRequestDTO;
import com.backendSiege.api.dto.auth.RegisterRequestDTO;
import com.backendSiege.domain.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import com.backendSiege.api.dto.auth.MeResponseDTO;
import com.backendSiege.infrastructure.entity.UserEntity;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponseDTO register(@RequestBody RegisterRequestDTO request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(
            @RequestBody LoginRequestDTO request,
            HttpServletResponse response
    ) {

        AuthResponseDTO authResponse = authService.login(request);

        Cookie cookie = new Cookie("token", authResponse.getToken());

        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 jour

        response.addCookie(cookie);

        return authResponse;
    }
    @GetMapping("/me")
    public MeResponseDTO me(Authentication authentication) {

        UserEntity user = authService.getCurrentUser(authentication.getName());

        return new MeResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getRoles(),
                user.getCountryId()
        );
    }
}