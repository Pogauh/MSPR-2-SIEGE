package com.backendSiege.domain.model;

import java.util.List;
import java.util.UUID;

public class User {
    private UUID id;
    private String email;
    private String password;
    private List<String> roles;

    public User(UUID id, String email, String password, List<String> roles) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public List<String> getRoles() { return roles; }
}
