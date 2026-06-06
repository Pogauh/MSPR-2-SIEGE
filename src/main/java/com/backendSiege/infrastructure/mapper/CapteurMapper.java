package com.backendSiege.infrastructure.mapper;

import com.backendSiege.api.dto.CapteurSummaryResponseDto;
import com.backendSiege.domain.model.CapteurSummary;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CapteurMapper {

    CapteurSummaryResponseDto toResponse(CapteurSummary capteurSummary);
}