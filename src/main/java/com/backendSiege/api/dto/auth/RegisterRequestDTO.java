package com.backendSiege.api.dto.auth;

import com.backendSiege.domain.models.Role;

/** Corps de {@code POST /api/auth/register} ; sans rôle, l'utilisateur reçoit {@code ROLE_HQ}. */
public record RegisterRequestDTO(String email, String password, Role role) {
}
