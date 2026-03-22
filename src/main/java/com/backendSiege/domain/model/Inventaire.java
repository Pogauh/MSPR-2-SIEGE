package com.backendSiege.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Inventaire {

    private final UUID id;
    private final String status;
    private final Integer quantite;
    private final Integer caracteristique;
    private final LocalDateTime dateSortie;
    private final LocalDateTime dateStockage;
    private final Entrepot entrepot;
    private final Exploitation exploitation;
    private final Pays pays;
}

