package com.backendSiege.api.mapper;

import com.backendSiege.api.dto.InventaireDto;
import com.backendSiege.domain.model.Inventaire;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventaireDtoMapper {

    @Mapping(target = "entrepotNom", source = "entrepot.nom")
    @Mapping(target = "entrepotLocalisation", source = "entrepot.localisation")
    @Mapping(target = "exploitationNom", source = "exploitation.nom")
    @Mapping(target = "exploitationAdresse", source = "exploitation.adresse")
    @Mapping(target = "paysNom", source = "pays.nom")
    InventaireDto toDto(Inventaire inventaire);

    List<InventaireDto> toDto(List<Inventaire> inventaires);
}

