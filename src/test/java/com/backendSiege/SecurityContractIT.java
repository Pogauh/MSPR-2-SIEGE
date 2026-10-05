package com.backendSiege;

import com.backendSiege.support.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.backendSiege.support.Donnees.*;
import static com.backendSiege.support.Json.json;
import static org.assertj.core.api.Assertions.assertThat;

/** Matrice des rôles : quelle route est ouverte à quel rôle (refus = 403, comme pour toutes les erreurs du siège). */
class SecurityContractIT extends IntegrationTestBase {

    private static final String ADMIN = "ROLE_ADMIN";
    private static final String HQ = "ROLE_HQ";
    private static final String ENTREPOT_RESP = "ROLE_WAREHOUSE_MANAGER";
    private static final String QUALITE = "ROLE_QUALITY";
    private static final String FERME = "ROLE_FARM_MANAGER";
    private static final String SUPPLY = "ROLE_SUPPLY_CHAIN";
    private static final List<String> TOUS = List.of(ADMIN, HQ, ENTREPOT_RESP, QUALITE, FERME, SUPPLY);

    record Cas(String methode, String chemin, String corps, Set<String> rolesAutorises) {
        @Override
        public String toString() {
            return methode + " " + chemin;
        }
    }

    static Stream<Cas> routes() {
        String periode = "pays=bresil&" + PERIODE;
        Set<String> tous = Set.copyOf(TOUS);
        return Stream.of(
                new Cas("GET", "/countries", null, tous),
                new Cas("GET", "/farms/bresil", null, tous),
                new Cas("GET", "/warehouses/bresil", null, tous),
                new Cas("GET", "/warehouses/" + ID_ENTREPOT + "/zones?pays=bresil", null, tous),
                new Cas("GET", "/warehouses/" + ID_ENTREPOT + "/readings/summary?pays=bresil", null, tous),
                new Cas("GET", "/readings/" + ID_ZONE + "/?" + periode, null, tous),
                new Cas("GET", "/zones/" + ID_ZONE + "/readings/latest?pays=bresil", null, tous),
                new Cas("GET", "/api/mesures/" + ID_MESURE + "?pays=bresil", null, tous),
                new Cas("GET", "/lots", null, tous),
                new Cas("GET", "/lot/" + ID_LOT, null, tous),
                new Cas("GET", "/lot/" + ID_LOT + "/mesures", null, tous),
                new Cas("GET", "/lot/oldest", null, tous),
                new Cas("GET", "/api/lot/total", null, tous),
                new Cas("GET", "/history/" + ID_LOT, null, tous),
                new Cas("GET", "/alertes?idEntrepot=" + ID_ENTREPOT, null, Set.of(ADMIN, ENTREPOT_RESP, FERME, QUALITE, SUPPLY)),
                new Cas("POST", "/lot?pays=bresil", json("{'status':'COMPLIANT'}"), Set.of(ADMIN, FERME)),
                new Cas("PUT", "/lot/" + ID_LOT + "/status?pays=bresil", json("{'status':'BLOCKED'}"), Set.of(ADMIN, ENTREPOT_RESP, QUALITE)),
                new Cas("PUT", "/lot/" + ID_LOT + "/zones?pays=bresil", json("{'idZone':'%s'}").formatted(ID_ZONE), Set.of(ADMIN, ENTREPOT_RESP)),
                new Cas("POST", "/mouvements/stockout?idLot=" + ID_LOT + "&pays=bresil", null, Set.of(ADMIN, ENTREPOT_RESP, FERME)),
                new Cas("GET", "/reporting/global", null, Set.of(ADMIN, HQ)),
                new Cas("GET", "/reporting/quality?" + periode, null, Set.of(ADMIN, QUALITE, HQ)),
                new Cas("GET", "/reporting/stock?" + periode, null, Set.of(ADMIN, SUPPLY, HQ)));
    }

    /** Un Brésil qui répond correctement partout : un appel autorisé ne doit alors jamais finir en 403. */
    @BeforeEach
    void bresilRepondPartout() {
        BRESIL.get("/api/exploitation/allExploitations", "[]");
        BRESIL.get("/api/entrepots", "[]");
        BRESIL.get("/api/entrepots/" + ID_ENTREPOT + "/zones", "[]");
        BRESIL.get("/api/entrepots/" + ID_ENTREPOT + "/capteurSummary", "[]");
        BRESIL.get("/api/zone/readings/" + ID_ZONE, "[]");
        BRESIL.get("/api/zone/" + ID_ZONE + "/readings/latest", mesure(ID_MESURE));
        BRESIL.get("/api/mesures/getMesure/" + ID_MESURE, mesure(ID_MESURE));
        BRESIL.get("/api/lots/allLotsFromAllEntrepots", "[" + lot(ID_LOT, "2026-01-10T08:30:00") + "]");
        BRESIL.get("/api/lots/getLot/" + ID_LOT, lot(ID_LOT, "2026-01-10T08:30:00"));
        BRESIL.get("/api/lots/getAllMesuresByLotId/" + ID_LOT, "[]");
        BRESIL.get("/api/lots/oldestLot", lot(ID_LOT, "2026-01-10T08:30:00"));
        BRESIL.get("/api/lots/getAllQuantity", "120");
        BRESIL.get("/api/historique/allHistoriquesForLot/" + ID_LOT, "[]");
        BRESIL.get("/api/alertes", "[]");
        BRESIL.repond("POST", "/api/lots", 201, json("{'idLot':'%s','status':'CREATED'}").formatted(ID_LOT));
        BRESIL.get("/api/reporting/global", json("{'totalLots':1,'avgAgeDays':1.0,'complianceRate':1.0,'countMouvementLast30Days':1}"));
        BRESIL.get("/api/reporting/quality", json("{'alertCount':1,'complianceRate':1.0,'nonComplianceCount':1}"));
        BRESIL.get("/api/reporting/stock", json("{'totalLots':1,'avgAgeDays':1.0,'complianceRate':1.0,'lotIdsUrgentFifo':[]}"));
        BRESIL.repond("PUT", "/api/lots/" + ID_LOT + "/status", 200, json("{'idLot':'%s','status':'OK'}").formatted(ID_LOT));
        BRESIL.repond("PUT", "/api/lots/" + ID_LOT + "/zone", 200, json("{'idLot':'%s','status':'OK'}").formatted(ID_LOT));
        BRESIL.repond("POST", "/api/lots/stock-out", 201, json("{'idHistorique':'%s','status':'CREATED'}").formatted(ID_HISTORIQUE));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("routes")
    void chaque_route_est_reservee_aux_roles_autorises(Cas cas) {
        for (String role : TOUS) {
            Reponse reponse = appeler(cas.methode(), cas.chemin(), jetonDuRole(role), cas.corps());

            if (cas.rolesAutorises().contains(role)) {
                assertThat(reponse.status()).as("%s doit être autorisé sur %s", role, cas).isNotEqualTo(403);
            } else {
                assertThat(reponse.status()).as("%s doit être refusé sur %s", role, cas).isEqualTo(403);
                assertThat(reponse.corps()).isEmpty();
            }
        }
    }

    @Test
    void la_route_est_refusee_sans_jeton() {
        for (Cas cas : routes().toList()) {
            assertThat(appeler(cas.methode(), cas.chemin(), null, cas.corps()).status()).as(cas.toString()).isEqualTo(403);
        }
    }
}
