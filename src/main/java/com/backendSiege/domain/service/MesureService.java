package com.backendSiege.domain.service;

import com.backendSiege.domain.port.MesurePort;
import com.backendSiege.api.dto.MesureResponseDto;
import com.backendSiege.domain.model.CapteurSummary;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@AllArgsConstructor
@Service
public class MesureService {

    private final MesurePort mesurePort;

    public MesureResponseDto getMesureById(String pays, UUID id) {
        return mesurePort.findMesureById(pays, id);
    }

    public List<MesureResponseDto> getMesuresByZoneId(String pays, UUID idZone, LocalDateTime startDate, LocalDateTime endDate) {
        return mesurePort.findMesuresByZoneId(pays, idZone, startDate, endDate);
    }

    public MesureResponseDto getLastMesuresByZoneId(String pays, UUID idZone) {
        return mesurePort.findLastMesuresByZoneId(pays, idZone);
    }

    public List<CapteurSummary> getCapteurSummaryByEntrepotId(String pays,UUID idCapteur) {
        return mesurePort.findCapteurSummaryByEntrepotId(pays, idCapteur);
    }
}
