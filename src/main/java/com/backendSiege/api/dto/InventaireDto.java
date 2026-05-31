package com.backendSiege.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class InventaireDto {

    private UUID id;
    private String status;
    private Integer quantite;
    private Integer caracteristique;
    private LocalDateTime dateSortie;
    private LocalDateTime dateStockage;
    private String entrepot;
    private String entrepotLocalisation;
    private String exploitation;
    private String exploitationAdresse;
    private String pays;
}

