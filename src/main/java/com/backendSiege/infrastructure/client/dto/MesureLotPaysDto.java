package com.backendSiege.infrastructure.client.dto;

import java.time.LocalDateTime;

/** Mesure d'un lot telle que renvoyée par {@code /api/lots/getAllMesuresByLotId/{idLot}}. */
public record MesureLotPaysDto(Double temperature, Double humidite, LocalDateTime dateMesure) {
}
