package com.backendSiege.infrastructure.mapper;

import com.backendSiege.api.dto.HistoriqueResponseDto;
import com.backendSiege.domain.model.Historique;
import com.backendSiege.domain.model.TypeHistoriqueEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)

public interface HistoriqueMapper {
    @Mapping(source = "type", target = "type", qualifiedByName = "convertTypeHistoriqueEnumToString")
    HistoriqueResponseDto toHistoriqueResponseDto(Historique historique);

    @Named("convertTypeHistoriqueEnumToString")
    default String convertTypeHistoriqueEnumToString(TypeHistoriqueEnum type) {
        if (type == null) {
            return null;
        }
        return type.name();
    }
}