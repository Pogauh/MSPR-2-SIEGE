package com.backendSiege.api.mapper;

import com.backendSiege.api.dto.AlerteDto;
import com.backendSiege.domain.models.Alerte;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface AlerteMapper {

    AlerteDto toDto(Alerte alerte);
}
