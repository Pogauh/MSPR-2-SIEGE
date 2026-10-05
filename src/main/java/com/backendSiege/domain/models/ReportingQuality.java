package com.backendSiege.domain.models;

import java.util.List;

/** Indicateurs de qualité sur une période : alertes, taux de conformité et lots non conformes. */
public record ReportingQuality(int alertCount, double complianceRate, int nonComplianceCount) {

    /** Rapport d'un pays qui n'a pas répondu. */
    public static ReportingQuality vide() {
        return new ReportingQuality(0, 0, 0);
    }

    /**
     * Consolide les rapports des pays : alertes et lots non conformes sont additionnés. Le taux de conformité est la
     * moyenne glissante des taux non nuls, dans l'ordre des rapports : le premier taux non nul est retenu, puis
     * chaque nouveau taux est moyenné avec le résultat courant (ce n'est donc pas la moyenne arithmétique au-delà de deux pays).
     */
    public static ReportingQuality consolider(List<ReportingQuality> parPays) {
        int alertCount = 0;
        double complianceRate = 0;
        int nonComplianceCount = 0;
        for (ReportingQuality pays : parPays) {
            alertCount += pays.alertCount();
            nonComplianceCount += pays.nonComplianceCount();
            if (pays.complianceRate() > 0) {
                complianceRate = complianceRate == 0 ? pays.complianceRate() : (complianceRate + pays.complianceRate()) / 2.0;
            }
        }
        return new ReportingQuality(alertCount, complianceRate, nonComplianceCount);
    }
}
