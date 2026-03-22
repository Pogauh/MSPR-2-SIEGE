package com.backendSiege.domain.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Capteur {

    private final UUID id;
    private final Integer humiAvg;
    private final String nom;
    private final String emplacementPrecis;
    private final String tempAvg;
    private final Entrepot entrepot;
}

