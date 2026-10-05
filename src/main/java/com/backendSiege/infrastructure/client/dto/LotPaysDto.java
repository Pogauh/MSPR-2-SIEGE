package com.backendSiege.infrastructure.client.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Lot tel que renvoyé par {@code /api/lots/...} d'un backend pays. */
public record LotPaysDto(UUID idLot, String status, Integer quantite, Integer caracteristique, LocalDateTime dateSortie,
                         LocalDateTime dateStockage, LocalDate dateProduction, String emplacement, UUID idExploitation,
                         UUID idEntrepot, List<HistoriquePaysDto> historique) {
}
