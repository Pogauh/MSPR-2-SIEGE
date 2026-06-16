package com.backendSiege.api.dto;

public class AuthResponseDTO {
    public String message;
    public String email;

    public AuthResponseDTO(String message, String email) {
        this.message = message;
        this.email = email;
    }
}