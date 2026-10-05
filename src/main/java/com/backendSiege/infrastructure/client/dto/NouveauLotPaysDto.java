package com.backendSiege.infrastructure.client.dto;

import java.time.LocalDate;
import java.util.UUID;

/** Corps de {@code POST /api/lots}. */
public record NouveauLotPaysDto(String status, Integer quantite, Integer caracteristique, LocalDate dateProduction,
                                UUID idExploitation, UUID idZone) {
}
