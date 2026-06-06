package com.backendSiege.infrastructure.mapper;

import com.backendSiege.api.dto.ZoneResponseDto;
import com.backendSiege.domain.model.ZoneResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ZoneMapper {

    ZoneResponseDto toDtoResponse(ZoneResponse zoneResponse);
}
