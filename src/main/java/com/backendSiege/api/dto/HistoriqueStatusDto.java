package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.UUID;

@JsonPropertyOrder({"idHistorique", "status"})
public record HistoriqueStatusDto(UUID idHistorique, String status) {
}
