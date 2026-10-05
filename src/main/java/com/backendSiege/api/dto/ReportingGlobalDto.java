package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"avgAgeDays", "complianceRate", "countMouvementLast30Days", "totalLots"})
public record ReportingGlobalDto(int totalLots, double avgAgeDays, double complianceRate, int countMouvementLast30Days) {
}
