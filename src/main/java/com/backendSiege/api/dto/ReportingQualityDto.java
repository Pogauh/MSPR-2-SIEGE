package com.backendSiege.api.dto;

import lombok.Data;

@Data
public class ReportingQualityDto {
    public int alertCount;
    public double complianceRate;
    public int nonComplianceCount;
}
