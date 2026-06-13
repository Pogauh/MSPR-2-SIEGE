package com.backendSiege.domain.port;

import com.backendSiege.api.dto.ReportingGlobalDto;
import com.backendSiege.api.dto.ReportingQualityDto;
import com.backendSiege.api.dto.ReportingStockDto;

import java.time.LocalDateTime;

public interface ReportingPort {

    ReportingGlobalDto findReportingGlobal();
    ReportingQualityDto findReportingQuality(String pays, LocalDateTime startDate, LocalDateTime endDate);
    ReportingStockDto findReportingStock(String pays, LocalDateTime startDate, LocalDateTime endDate);
}
