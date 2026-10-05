package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record CapteurSummaryResponseDto(
        @JsonProperty("zone_id") UUID zoneId,
        @JsonProperty("zone_name") String zoneName,
        @JsonProperty("last_temp") BigDecimal lastTemp,
        @JsonProperty("last_humidity") BigDecimal lastHumidity,
        @JsonProperty("avg_temp_24h") BigDecimal avgTemp24h,
        @JsonProperty("avg_humidity_24h") BigDecimal avgHumidity24h) {
}
