package com.backendSiege.api.mapper;

import com.backendSiege.api.dto.MesureResponseDto;
import com.backendSiege.domain.models.Mesure;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface MesureMapper {

    MesureResponseDto toResponse(Mesure mesure);
}
