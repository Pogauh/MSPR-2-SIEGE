package com.backendSiege.api.controller;

import com.backendSiege.api.dto.auth.AuthResponseDTO;
import com.backendSiege.api.dto.auth.LoginRequestDTO;
import com.backendSiege.api.dto.auth.MeResponseDTO;
import com.backendSiege.api.dto.auth.RegisterRequestDTO;
import com.backendSiege.api.mapper.UserMapper;
import com.backendSiege.domain.services.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private static final int DUREE_COOKIE_SECONDES = 24 * 60 * 60;

    private final AuthService authService;
    private final UserMapper userMapper;

    public AuthController(AuthService authService, UserMapper userMapper) {
        this.authService = authService;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public AuthResponseDTO register(@RequestBody RegisterRequestDTO request) {
        return new AuthResponseDTO(authService.register(request.email(), request.password(), request.role()));
    }

    /** Renvoie le jeton dans le corps et le dépose aussi dans le cookie {@code token} (HttpOnly, un jour). */
    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO request, HttpServletResponse response) {
        AuthResponseDTO authResponse = new AuthResponseDTO(authService.login(request.email(), request.password()));

        Cookie cookie = new Cookie("token", authResponse.token());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(DUREE_COOKIE_SECONDES);
        response.addCookie(cookie);

        return authResponse;
    }

    @GetMapping("/me")
    public MeResponseDTO me(Authentication authentication) {
        return userMapper.toMeResponse(authService.getCurrentUser(authentication.getName()));
    }
}
