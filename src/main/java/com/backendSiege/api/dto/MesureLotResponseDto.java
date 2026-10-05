package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;

@JsonPropertyOrder({"temperature", "humidite", "dateMesure"})
public record MesureLotResponseDto(Double temperature, Double humidite, LocalDateTime dateMesure) {
}
