package com.backendSiege.api.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/** Configuration commune des mappers modèle du domaine &lt;-&gt; DTO de l'API : un champ non mappé est une erreur de compilation. */
@MapperConfig(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ApiMapperConfig {
}
