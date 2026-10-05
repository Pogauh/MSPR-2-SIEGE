package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonPropertyOrder({"idMesure", "temperature", "humidite", "dateMesure", "idCapteur"})
public record MesureResponseDto(UUID idMesure, Double temperature, Double humidite, LocalDateTime dateMesure, UUID idCapteur) {
}
