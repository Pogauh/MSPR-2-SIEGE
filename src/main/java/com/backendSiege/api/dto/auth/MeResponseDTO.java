package com.backendSiege.api.dto.auth;

import com.backendSiege.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MeResponseDTO {

    private UUID id;
    private String email;
    private Set<Role> roles;
    private UUID countryId;
}