package com.backendSiege.domain.port;

import com.backendSiege.api.dto.MesureResponseDto;
import com.backendSiege.domain.model.CapteurSummary;

import java.util.List;
import java.util.UUID;

public interface MesurePort {
    MesureResponseDto findMesureById(String pays, UUID id);
    List<MesureResponseDto> findMesuresByCapteurId(String pays, UUID idCapteur);
    List<CapteurSummary> findCapteurSummaryByEntrepotId(String pays, UUID idEntrepot);
}
