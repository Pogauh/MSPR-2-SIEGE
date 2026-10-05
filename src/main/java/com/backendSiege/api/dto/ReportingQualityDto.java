package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"alertCount", "complianceRate", "nonComplianceCount"})
public record ReportingQualityDto(int alertCount, double complianceRate, int nonComplianceCount) {
}
