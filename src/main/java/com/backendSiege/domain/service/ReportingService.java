package com.backendSiege.domain.service;

import com.backendSiege.api.dto.ReportingGlobalDto;
import com.backendSiege.domain.port.ReportingPort;
import org.springframework.stereotype.Service;

@Service
public class ReportingService {

    private final ReportingPort reportingPort;

    public ReportingService(ReportingPort reportingPort) {
        this.reportingPort = reportingPort;
    }

    public ReportingGlobalDto getReportingGlobal() {
        return reportingPort.findReportingGlobal();
    }
}
