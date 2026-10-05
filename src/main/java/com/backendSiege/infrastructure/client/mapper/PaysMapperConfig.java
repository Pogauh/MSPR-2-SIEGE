package com.backendSiege.infrastructure.client.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/** Configuration commune des mappers DTO des backends pays &lt;-&gt; modèles du domaine : un champ non mappé est une erreur de compilation. */
@MapperConfig(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PaysMapperConfig {
}
