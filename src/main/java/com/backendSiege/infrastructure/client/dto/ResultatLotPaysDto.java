package com.backendSiege.infrastructure.client.dto;

import java.util.UUID;

/** Réponse d'un backend pays à la création, au changement de statut ou au transfert d'un lot. */
public record ResultatLotPaysDto(UUID idLot, String status) {
}
