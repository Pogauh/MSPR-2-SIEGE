package com.backendSiege.infrastructure.client.dto;

import java.util.List;
import java.util.UUID;

/** Rapport tel que renvoyé par {@code /api/reporting/stock}. */
public record ReportingStockPaysDto(int totalLots, double avgAgeDays, double complianceRate, List<UUID> lotIdsUrgentFifo) {
}
