package com.backendSiege.support;

import java.util.UUID;

import static com.backendSiege.support.Json.json;

/**
 * Identifiants et documents JSON du contrat des backends pays, partagés par les tests d'intégration.
 * Les documents sont écrits dans l'ordre des champs renvoyé par le siège : ils servent à la fois de réponse du faux pays
 * et de réponse attendue du siège quand celui-ci relaie la donnée telle quelle.
 */
public final class Donnees {

    public static final UUID ID_LOT = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID ID_LOT_COLOMBIE = UUID.fromString("55555555-5555-5555-5555-555555555555");
    public static final UUID ID_ZONE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID ID_ZONE_PRECEDENTE = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID ID_HISTORIQUE = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID ID_ENTREPOT = UUID.fromString("770e8400-e29b-41d4-a716-446655440011");
    public static final UUID ID_EXPLOITATION = UUID.fromString("aa0e8400-e29b-41d4-a716-446655440022");
    public static final UUID ID_CAPTEUR = UUID.fromString("b793e242-053a-4def-9cd8-96c990a586c9");
    public static final UUID ID_MESURE = UUID.fromString("99999999-9999-9999-9999-999999999999");
    public static final UUID ID_ALERTE = UUID.fromString("66666666-6666-6666-6666-666666666666");

    public static final String PERIODE = "startDate=2026-01-01T00:00&endDate=2026-01-31T00:00";

    private Donnees() {
    }

    public static String historique(String type) {
        return json("{'idHistorique':'%s','type':'%s','dateEmission':'2026-01-11T09:00:00','zonePrecedente':'%s',"
                + "'zoneActuelle':'%s','detail':'Transfert'}").formatted(ID_HISTORIQUE, type, ID_ZONE_PRECEDENTE, ID_ZONE);
    }

    /** Un lot tel que renvoyé par un backend pays ; {@code dateStockage} nul se donne par {@code null}. */
    public static String lot(UUID idLot, String dateStockage) {
        String date = dateStockage == null ? "null" : "'" + dateStockage + "'";
        return json("{'idLot':'%s','status':'COMPLIANT','quantite':120,'caracteristique':2,'dateSortie':null,"
                + "'dateStockage':%s,'dateProduction':'2025-12-01','emplacement':'%s','idExploitation':'%s',"
                + "'idEntrepot':'%s','historique':[%s]}")
                .formatted(idLot, date.replace('\'', '"'), ID_ZONE, ID_EXPLOITATION, ID_ENTREPOT, historique("TRANSFERT"));
    }

    public static String mesure(UUID idMesure) {
        return json("{'idMesure':'%s','temperature':27.5,'humidite':56.2,'dateMesure':'2026-01-12T10:00:00','idCapteur':'%s'}")
                .formatted(idMesure, ID_CAPTEUR);
    }

    public static String alerte() {
        return json("{'idAlerte':'%s','typeAlerte':'HAUTETEMPERATURE','description':'Température trop haute','dateEmission':'2026-01-12T10:05:00',"
                + "'idZone':'%s'}").formatted(ID_ALERTE, ID_ZONE);
    }
}
