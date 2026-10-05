package com.backendSiege.infrastructure.client.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** Mesure telle que renvoyée par un backend pays. */
public record MesurePaysDto(UUID idMesure, Double temperature, Double humidite, LocalDateTime dateMesure, UUID idCapteur) {
}
