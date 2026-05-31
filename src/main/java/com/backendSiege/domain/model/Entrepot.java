package com.backendSiege.domain.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Entrepot {

    private final UUID id;
    private final String localisation;
    private final String nom;
    private final String pays;
}

