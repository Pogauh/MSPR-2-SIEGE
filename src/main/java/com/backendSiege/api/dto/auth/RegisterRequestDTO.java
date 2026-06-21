package com.backendSiege.api.dto.auth;

import com.backendSiege.domain.model.Role;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String email;
    private String password;
    private Role role;
}