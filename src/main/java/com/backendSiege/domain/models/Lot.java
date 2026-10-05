package com.backendSiege.domain.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Lot de café tel que le connaît le backend de son pays.
 *
 * @param status      statut du lot (COMPLIANT, ALERT, BLOCKED, EXPIRED, SHIPPED…), relayé tel quel
 * @param emplacement identifiant de la zone où le lot est stocké, relayé tel quel
 */
public record Lot(UUID idLot, String status, Integer quantite, Integer caracteristique, LocalDateTime dateSortie,
                  LocalDateTime dateStockage, LocalDate dateProduction, String emplacement, UUID idExploitation,
                  UUID idEntrepot, List<Historique> historique) {

    /**
     * Le lot stocké depuis le plus longtemps des deux. Un lot sans date de stockage cède la place à l'autre ;
     * à date égale, c'est {@code autre} qui l'emporte.
     */
    public static Lot lePlusAncien(Lot lot, Lot autre) {
        if (lot.dateStockage() == null) {
            return autre;
        }
        if (autre.dateStockage() == null) {
            return lot;
        }
        return lot.dateStockage().isBefore(autre.dateStockage()) ? lot : autre;
    }
}
