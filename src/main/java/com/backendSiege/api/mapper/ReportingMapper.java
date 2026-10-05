package com.backendSiege.api.mapper;

import com.backendSiege.api.dto.ReportingGlobalDto;
import com.backendSiege.api.dto.ReportingQualityDto;
import com.backendSiege.api.dto.ReportingStockDto;
import com.backendSiege.domain.models.ReportingGlobal;
import com.backendSiege.domain.models.ReportingQuality;
import com.backendSiege.domain.models.ReportingStock;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface ReportingMapper {

    ReportingGlobalDto toDto(ReportingGlobal reporting);

    ReportingQualityDto toDto(ReportingQuality reporting);

    ReportingStockDto toDto(ReportingStock reporting);
}
