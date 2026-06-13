package com.backendSiege.domain.port;

import com.backendSiege.api.dto.ReportingGlobalDto;
import com.backendSiege.api.dto.ReportingQualityDto;

import java.time.LocalDateTime;

public interface ReportingPort {

    ReportingGlobalDto findReportingGlobal();
    ReportingQualityDto findReportingQuality(LocalDateTime startDate, LocalDateTime endDate);
}
