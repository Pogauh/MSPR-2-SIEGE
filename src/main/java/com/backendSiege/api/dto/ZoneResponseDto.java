package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.UUID;

@JsonPropertyOrder({"idZone", "nomZone", "idEntrepot", "idCapteur", "lotCount"})
public record ZoneResponseDto(UUID idZone, String nomZone, UUID idEntrepot, UUID idCapteur, Long lotCount) {
}
