package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.UUID;

@JsonPropertyOrder({"totalLots", "avgAgeDays", "complianceRate", "lotIdsUrgentFifo"})
public record ReportingStockDto(int totalLots, double avgAgeDays, double complianceRate, List<UUID> lotIdsUrgentFifo) {
}
