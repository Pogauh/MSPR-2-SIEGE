package com.backendSiege.api.dto;

import java.time.LocalDate;
import java.util.UUID;

/** Corps de {@code POST /lot} : le lot à créer. */
public record LotRequestDto(String status, Integer quantite, Integer caracteristique, LocalDate dateProduction,
                            UUID idExploitation, UUID idZone) {
}
