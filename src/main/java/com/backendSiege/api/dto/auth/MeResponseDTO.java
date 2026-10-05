package com.backendSiege.api.dto.auth;

import com.backendSiege.domain.models.Role;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Set;
import java.util.UUID;

@JsonPropertyOrder({"id", "email", "roles", "countryId"})
public record MeResponseDTO(UUID id, String email, Set<Role> roles, String countryId) {
}
