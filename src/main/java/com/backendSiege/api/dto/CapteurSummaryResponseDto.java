package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CapteurSummaryResponseDto {
    @JsonProperty("zone_id")
    private UUID zoneId;

    @JsonProperty("zone_name")
    private String zoneName;

    @JsonProperty("last_temp")
    private BigDecimal lastTemp;

    @JsonProperty("last_humidity")
    private BigDecimal lastHumidity;

    @JsonProperty("avg_temp_24h")
    private BigDecimal avgTemp24h;

    @JsonProperty("avg_humidity_24h")
    private BigDecimal avgHumidity24h;
}