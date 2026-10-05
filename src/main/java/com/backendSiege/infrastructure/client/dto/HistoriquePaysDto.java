package com.backendSiege.infrastructure.client.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** Événement d'historique tel que renvoyé par un backend pays. */
public record HistoriquePaysDto(UUID idHistorique, String type, LocalDateTime dateEmission, UUID zonePrecedente,
                                UUID zoneActuelle, String detail) {
}
