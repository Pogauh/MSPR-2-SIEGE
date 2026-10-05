package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonPropertyOrder({"idLot", "status", "quantite", "caracteristique", "dateSortie", "dateStockage", "dateProduction",
        "emplacement", "idExploitation", "idEntrepot", "historique"})
public record LotResponseDto(UUID idLot, String status, Integer quantite, Integer caracteristique, LocalDateTime dateSortie,
                             LocalDateTime dateStockage, LocalDate dateProduction, String emplacement, UUID idExploitation,
                             UUID idEntrepot, List<HistoriqueResponseDto> historique) {
}
