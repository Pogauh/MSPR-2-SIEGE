package com.backendSiege.domain.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Pays {

    private final UUID id;
    private final String nom;
}

