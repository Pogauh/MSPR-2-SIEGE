package com.backendSiege.api.controller;

import com.backendSiege.api.dto.ReportingGlobalDto;
import com.backendSiege.api.dto.ReportingQualityDto;
import com.backendSiege.api.dto.ReportingStockDto;
import com.backendSiege.api.mapper.ReportingMapper;
import com.backendSiege.domain.services.ReportingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class ReportingController {

    private final ReportingService reportingService;
    private final ReportingMapper reportingMapper;

    public ReportingController(ReportingService reportingService, ReportingMapper reportingMapper) {
        this.reportingService = reportingService;
        this.reportingMapper = reportingMapper;
    }

    @GetMapping("/reporting/global")
    public ReportingGlobalDto getGlobalReport() {
        return reportingMapper.toDto(reportingService.getReportingGlobal());
    }

    /** Qualité sur une période, d'un pays ou (sans {@code pays}) consolidée sur tous les pays. */
    @GetMapping("/reporting/quality")
    public ReportingQualityDto getQualityReport(@RequestParam(value = "pays", required = false) String pays,
                                                @RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return reportingMapper.toDto(reportingService.getReportingQuality(pays, startDate, endDate));
    }

    /** État du stock d'un pays (le pays est obligatoire). */
    @GetMapping("/reporting/stock")
    public ReportingStockDto getStockReport(@RequestParam String pays, @RequestParam LocalDateTime startDate,
                                            @RequestParam LocalDateTime endDate) {
        return reportingMapper.toDto(reportingService.getReportingStock(pays, startDate, endDate));
    }
}
