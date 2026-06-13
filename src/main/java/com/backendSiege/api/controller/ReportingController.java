package com.backendSiege.api.controller;

import com.backendSiege.api.dto.ReportingGlobalDto;
import com.backendSiege.api.dto.ReportingQualityDto;
import com.backendSiege.domain.service.ReportingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController

public class ReportingController {
     private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/reporting/global")
    public ReportingGlobalDto getGlobalReport() {
        return reportingService.getReportingGlobal();
    }

    @GetMapping("/reporting/quality")
    public ReportingQualityDto getQualityReport(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return reportingService.getReportingQuality(startDate, endDate);
    }

}
