package com.backendSiege.domain.models;

import java.util.List;

/** Indicateurs globaux du stock : nombre de lots, âge moyen, taux de conformité et mouvements des 30 derniers jours. */
public record ReportingGlobal(int totalLots, double avgAgeDays, double complianceRate, int countMouvementLast30Days) {

    /** Rapport d'un pays sans lot ou qui n'a pas répondu. */
    public static ReportingGlobal vide() {
        return new ReportingGlobal(0, 0, 0, 0);
    }

    /**
     * Consolide les rapports des pays : lots et mouvements sont additionnés, l'âge moyen et le taux de conformité
     * sont pondérés par le nombre de lots de chaque pays.
     */
    public static ReportingGlobal consolider(List<ReportingGlobal> parPays) {
        int totalLots = 0;
        double avgAgeDays = 0;
        double complianceRate = 0;
        int mouvements = 0;
        for (ReportingGlobal pays : parPays) {
            int lotsAvant = totalLots;
            int lotsDuPays = pays.totalLots();
            totalLots = lotsAvant + lotsDuPays;
            mouvements += pays.countMouvementLast30Days();
            if (totalLots > 0) {
                avgAgeDays = (avgAgeDays * lotsAvant + pays.avgAgeDays() * lotsDuPays) / totalLots;
                complianceRate = (complianceRate * lotsAvant + pays.complianceRate() * lotsDuPays) / totalLots;
            }
        }
        return new ReportingGlobal(totalLots, avgAgeDays, complianceRate, mouvements);
    }
}
