package com.backendSiege.domain.ports;

import com.backendSiege.domain.models.ReportingGlobal;
import com.backendSiege.domain.models.ReportingQuality;
import com.backendSiege.domain.models.ReportingStock;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Rapports des backends des pays. Les pays sont désignés par leur code normalisé et interrogés en parallèle.
 * Un pays qui répond en erreur ou qui est injoignable donne un rapport vide.
 */
public interface ReportingPort {

    /** Le rapport global de chaque pays interrogé. */
    List<ReportingGlobal> findReportingGlobal(Collection<String> codesPays);

    /** Le rapport de qualité de chaque pays interrogé. */
    List<ReportingQuality> findReportingQuality(Collection<String> codesPays, LocalDateTime debut, LocalDateTime fin);

    ReportingStock findReportingStock(String codePays, LocalDateTime debut, LocalDateTime fin);
}
