package com.backendSiege.domain.service;

import com.backendSiege.api.dto.ReportingGlobalDto;
import com.backendSiege.api.dto.ReportingQualityDto;
import com.backendSiege.api.dto.ReportingStockDto;
import com.backendSiege.domain.port.ReportingPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportingService {

    private final ReportingPort reportingPort;

    public ReportingService(ReportingPort reportingPort) {
        this.reportingPort = reportingPort;
    }

    public ReportingGlobalDto getReportingGlobal() {
        return reportingPort.findReportingGlobal();
    }

    public ReportingQualityDto getReportingQuality(String pays, LocalDateTime startDate, LocalDateTime endDate) {
        return reportingPort.findReportingQuality(pays, startDate, endDate);
    }

    public ReportingStockDto getReportingStock(String pays, LocalDateTime startDate, LocalDateTime endDate) {
        return reportingPort.findReportingStock(pays, startDate, endDate);
    }
}
