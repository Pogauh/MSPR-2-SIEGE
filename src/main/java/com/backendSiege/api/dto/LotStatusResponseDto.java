package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.UUID;

@JsonPropertyOrder({"idLot", "status"})
public record LotStatusResponseDto(UUID idLot, String status) {
}
