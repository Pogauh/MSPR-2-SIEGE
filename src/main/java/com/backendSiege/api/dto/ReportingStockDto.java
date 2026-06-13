package com.backendSiege.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@JsonPropertyOrder({
        "totalLots",
        "avgAgeDays",
        "complianceRate",
        "caracteristique",
        "lotIdsUrgentFifo"
})
public class ReportingStockDto {
    public int totalLots;
    public double avgAgeDays;
    public double complianceRate;
    public List<UUID> lotIdsUrgentFifo;
}
