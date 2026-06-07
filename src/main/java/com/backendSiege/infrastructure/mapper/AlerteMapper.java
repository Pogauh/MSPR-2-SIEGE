package com.backendSiege.infrastructure.mapper;

import com.backendSiege.api.dto.AlerteDto;
import com.backendSiege.domain.model.Alerte;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlerteMapper {
    AlerteDto toAlerteDto(Alerte alerte);
}
