package com.backendSiege.api.dto;

import lombok.Data;

@Data
public class ReportingGlobalDto {
    public int totalLots;
    public double avgAgeDays;
    public double complianceRate;
    public int countMouvementLast30Days;
}
