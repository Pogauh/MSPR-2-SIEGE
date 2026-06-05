package com.backendSiege.domain.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Entrepot {

    private final UUID idEntrepot;
    private final String localisation;
    private final String nom;
}

