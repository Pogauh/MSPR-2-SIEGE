package com.backendSiege.infrastructure.client.dto;

import java.util.UUID;

/** Entrepôt tel que renvoyé par {@code /api/entrepots}. */
public record EntrepotPaysDto(UUID idEntrepot, String localisation, String nom) {
}
