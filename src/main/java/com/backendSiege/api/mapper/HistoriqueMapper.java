package com.backendSiege.api.mapper;

import com.backendSiege.api.dto.HistoriqueResponseDto;
import com.backendSiege.domain.models.Historique;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface HistoriqueMapper {

    HistoriqueResponseDto toResponse(Historique historique);
}
