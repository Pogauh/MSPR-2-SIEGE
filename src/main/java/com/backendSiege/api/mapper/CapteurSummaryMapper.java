package com.backendSiege.api.mapper;

import com.backendSiege.api.dto.CapteurSummaryResponseDto;
import com.backendSiege.domain.models.CapteurSummary;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface CapteurSummaryMapper {

    CapteurSummaryResponseDto toResponse(CapteurSummary capteurSummary);
}
