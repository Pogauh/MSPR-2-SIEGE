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
    private String entrepotNom;
    private String entrepotLocalisation;
    private String exploitationNom;
    private String exploitationAdresse;
    private String paysNom;
}

