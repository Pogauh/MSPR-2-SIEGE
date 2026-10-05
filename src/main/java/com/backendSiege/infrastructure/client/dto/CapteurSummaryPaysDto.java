package com.backendSiege.infrastructure.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Résumé d'une zone tel que renvoyé par {@code /api/entrepots/{id}/capteurSummary}. */
public record CapteurSummaryPaysDto(UUID zoneId, String zoneName, BigDecimal lastTemp, BigDecimal lastHumidity,
                                    BigDecimal avgTemp24h, BigDecimal avgHumidity24h) {
}
