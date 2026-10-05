package com.backendSiege.api.mapper;

import com.backendSiege.api.dto.EntrepotDto;
import com.backendSiege.api.dto.ExploitationDto;
import com.backendSiege.api.dto.PaysDto;
import com.backendSiege.api.dto.ZoneResponseDto;
import com.backendSiege.domain.models.Entrepot;
import com.backendSiege.domain.models.Exploitation;
import com.backendSiege.domain.models.Pays;
import com.backendSiege.domain.models.Tolerances;
import com.backendSiege.domain.models.Zone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = ApiMapperConfig.class)
public interface ReferentielMapper {

    ExploitationDto toDto(Exploitation exploitation);

    EntrepotDto toDto(Entrepot entrepot);

    ZoneResponseDto toResponse(Zone zone);

    @Mapping(target = "tolerences", source = "tolerances")
    PaysDto toDto(Pays pays);

    PaysDto.TolerancesDto toDto(Tolerances tolerances);
}
