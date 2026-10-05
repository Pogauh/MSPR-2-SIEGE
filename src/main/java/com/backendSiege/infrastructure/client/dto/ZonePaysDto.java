package com.backendSiege.infrastructure.client.dto;

import java.util.UUID;

/** Zone telle que renvoyée par {@code /api/entrepots/{id}/zones}. */
public record ZonePaysDto(UUID idZone, String nomZone, UUID idEntrepot, UUID idCapteur, Long lotCount) {
}
