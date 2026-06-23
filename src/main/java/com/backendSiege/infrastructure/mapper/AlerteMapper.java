package com.backendSiege.infrastructure.mapper;

import com.backendSiege.api.dto.AlerteDto;
import com.backendSiege.domain.model.Alerte;
import org.springframework.stereotype.Component;

@Component
public class AlerteMapper {

    public AlerteDto toAlerteDto(Alerte alerte) {
        if (alerte == null) {
            return null;
        }

        AlerteDto dto = new AlerteDto();

        // Mets ici les vrais champs de AlerteDto / Alerte
        return dto;
    }
}