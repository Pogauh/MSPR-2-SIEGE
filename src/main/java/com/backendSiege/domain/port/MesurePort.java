package com.backendSiege.domain.port;

import com.backendSiege.api.dto.MesureResponseDto;
import com.backendSiege.domain.model.CapteurSummary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MesurePort {
    MesureResponseDto findMesureById(String pays, UUID id);
    List<MesureResponseDto> findMesuresByZoneId(String pays, UUID idZone, LocalDateTime startDate, LocalDateTime endDate);

    MesureResponseDto findLastMesuresByZoneId(String pays, UUID idZone);
    List<CapteurSummary> findCapteurSummaryByEntrepotId(String pays, UUID idEntrepot);
}
