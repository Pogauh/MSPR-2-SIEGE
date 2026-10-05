package com.backendSiege.domain.services;

import com.backendSiege.domain.exceptions.PaysIntrouvableException;
import com.backendSiege.domain.exceptions.PaysNonSupporteException;
import com.backendSiege.domain.exceptions.PaysObligatoireException;
import com.backendSiege.domain.models.ReportingGlobal;
import com.backendSiege.domain.models.ReportingQuality;
import com.backendSiege.domain.models.ReportingStock;
import com.backendSiege.domain.ports.ReportingPort;

import java.time.LocalDateTime;
import java.util.List;

/** Reporting du siège : consolidation des indicateurs des pays. */
public class ReportingService {

    private final ReportingPort reportingPort;
    private final PaysService paysService;

    public ReportingService(ReportingPort reportingPort, PaysService paysService) {
        this.reportingPort = reportingPort;
        this.paysService = paysService;
    }

    /** Indicateurs globaux de tous les pays. */
    public ReportingGlobal getReportingGlobal() {
        return ReportingGlobal.consolider(reportingPort.findReportingGlobal(paysService.codes()));
    }

    /**
     * Indicateurs de qualité du pays désigné, ou consolidés sur tous les pays si la saisie est absente ou vide.
     *
     * @throws PaysNonSupporteException si le pays désigné n'est pas configuré
     */
    public ReportingQuality getReportingQuality(String pays, LocalDateTime debut, LocalDateTime fin) {
        if (pays == null || pays.isBlank()) {
            return ReportingQuality.consolider(reportingPort.findReportingQuality(paysService.codes(), debut, fin));
        }
        String code = paysService.resoudre(pays).orElseThrow(() -> new PaysNonSupporteException(pays));
        List<ReportingQuality> rapports = reportingPort.findReportingQuality(List.of(code), debut, fin);
        return rapports.isEmpty() ? ReportingQuality.vide() : rapports.get(0);
    }

    /**
     * État du stock du pays désigné.
     *
     * @throws PaysObligatoireException si aucun pays n'est désigné
     * @throws PaysIntrouvableException si le pays désigné n'est pas configuré
     */
    public ReportingStock getReportingStock(String pays, LocalDateTime debut, LocalDateTime fin) {
        if (pays == null || pays.isBlank()) {
            throw new PaysObligatoireException();
        }
        String code = paysService.resoudre(pays).orElseThrow(() -> new PaysIntrouvableException(pays));
        return reportingPort.findReportingStock(code, debut, fin);
    }
}
