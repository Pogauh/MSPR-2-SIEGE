package com.backendSiege.infrastructure.client.dto;

import java.time.LocalDateTime;

/** Alerte telle que renvoyée par {@code /api/alertes}. */
public record AlertePaysDto(String idAlerte, String typeAlerte, String description, LocalDateTime dateEmission, String idZone) {
}
