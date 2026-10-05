package com.backendSiege.infrastructure.client.dto;

import java.util.UUID;

/** Réponse d'un backend pays à une sortie de stock. */
public record ResultatSortieStockPaysDto(UUID idHistorique, String status) {
}
