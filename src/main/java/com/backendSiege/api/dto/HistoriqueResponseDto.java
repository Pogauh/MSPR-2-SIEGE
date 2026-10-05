package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonPropertyOrder({"idHistorique", "type", "dateEmission", "zonePrecedente", "zoneActuelle", "detail"})
public record HistoriqueResponseDto(UUID idHistorique, String type, LocalDateTime dateEmission, UUID zonePrecedente,
                                    UUID zoneActuelle, String detail) {
}
