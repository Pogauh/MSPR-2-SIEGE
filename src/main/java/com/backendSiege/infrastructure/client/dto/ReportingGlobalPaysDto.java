package com.backendSiege.infrastructure.client.dto;

/** Rapport tel que renvoyé par {@code /api/reporting/global}. */
public record ReportingGlobalPaysDto(int totalLots, double avgAgeDays, double complianceRate, int countMouvementLast30Days) {
}
