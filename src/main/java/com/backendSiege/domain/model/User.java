package com.backendSiege.domain.model;

import lombok.Data;

import java.util.UUID;

@Data
public class User {

    private UUID id;

    private String email;

    private String password;

    private Role role;

    private Pays pays;
}