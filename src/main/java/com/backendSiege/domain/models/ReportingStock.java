package com.backendSiege.domain.models;

import java.util.List;
import java.util.UUID;

/**
 * État du stock d'un pays.
 *
 * @param lotIdsUrgentFifo lots à écouler en priorité ; {@code null} si le pays n'a pas répondu
 */
public record ReportingStock(int totalLots, double avgAgeDays, double complianceRate, List<UUID> lotIdsUrgentFifo) {

    /** Rapport d'un pays qui n'a pas répondu : des zéros et pas de liste de lots urgents. */
    public static ReportingStock vide() {
        return new ReportingStock(0, 0, 0, null);
    }
}
