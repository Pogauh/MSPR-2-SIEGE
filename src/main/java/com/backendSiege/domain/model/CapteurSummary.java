package com.backendSiege.domain.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CapteurSummary {
    private UUID zoneId;
    private String zoneName;
    private BigDecimal lastTemp;
    private BigDecimal lastHumidity;
    private BigDecimal avgTemp24h;
    private BigDecimal avgHumidity24h;

    public CapteurSummary(UUID zoneId, String zoneName, BigDecimal lastTemp, BigDecimal lastHumidity, BigDecimal avgTemp24h, BigDecimal avgHumidity24h) {
        this.zoneId = zoneId;
        this.zoneName = zoneName;
        this.lastTemp = lastTemp;
        this.lastHumidity = lastHumidity;
        this.avgTemp24h = avgTemp24h;
        this.avgHumidity24h = avgHumidity24h;
    }
}