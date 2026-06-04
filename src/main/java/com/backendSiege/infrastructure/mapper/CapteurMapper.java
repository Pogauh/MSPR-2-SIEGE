package com.backendSiege.infrastructure.mapper;

import com.backendSiege.api.dto.CapteurSummaryResponseDto;
import com.backendSiege.domain.model.CapteurSummary;
import org.springframework.stereotype.Component;

@Component
public class CapteurMapper {

    public CapteurSummaryResponseDto toResponse(CapteurSummary capteurSummary) {
        CapteurSummaryResponseDto dto = new CapteurSummaryResponseDto();
        dto.setZoneId(capteurSummary.getZoneId());
        dto.setZoneName(capteurSummary.getZoneName());
        dto.setAvgTemp24h(capteurSummary.getAvgTemp24h());
        dto.setAvgHumidity24h(capteurSummary.getAvgHumidity24h());
        dto.setLastHumidity(capteurSummary.getLastHumidity());
        dto.setLastTemp(capteurSummary.getLastTemp());
        return dto;
    }
}