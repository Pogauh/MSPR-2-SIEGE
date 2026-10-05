package com.backendSiege.infrastructure.client.dto;

/** Rapport tel que renvoyé par {@code /api/reporting/quality}. */
public record ReportingQualityPaysDto(int alertCount, double complianceRate, int nonComplianceCount) {
}
