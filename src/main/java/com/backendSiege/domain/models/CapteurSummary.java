package com.backendSiege.domain.models;

import java.math.BigDecimal;
import java.util.UUID;

/** Dernière mesure et moyennes sur 24 h du capteur d'une zone. */
public record CapteurSummary(UUID zoneId, String zoneName, BigDecimal lastTemp, BigDecimal lastHumidity,
                             BigDecimal avgTemp24h, BigDecimal avgHumidity24h) {
}
