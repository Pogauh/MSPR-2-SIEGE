package com.backendSiege;

import com.backendSiege.support.FakePaysServer.RequestRecue;
import com.backendSiege.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static com.backendSiege.support.Json.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

/** Contrat du reporting : consolidation des indicateurs des pays. */
class ReportingContractIT extends IntegrationTestBase {

    private static final String PERIODE = "startDate=2026-01-01T00:00:00&endDate=2026-01-31T00:00:00";
    private static final String PERIODE_RELAYEE = "startDate=2026-01-01T00:00&endDate=2026-01-31T00:00";

    // ---------------------------------------------------------------- global

    @Test
    void reporting_global_somme_les_lots_et_les_mouvements_et_pondere_age_et_conformite_par_le_nombre_de_lots() {
        BRESIL.get("/api/reporting/global", json("{'totalLots':10,'avgAgeDays':30.0,'complianceRate':80.0,'countMouvementLast30Days':4}"));
        COLOMBIE.get("/api/reporting/global", json("{'totalLots':30,'avgAgeDays':50.0,'complianceRate':100.0,'countMouvementLast30Days':6}"));

        Reponse reponse = get("/reporting/global", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(json("{'avgAgeDays':45.0,'complianceRate':95.0,'countMouvementLast30Days':10,'totalLots':40}"));
        for (var pays : TOUS_LES_PAYS) {
            assertThat(pays.requetes()).extracting(RequestRecue::chemin, RequestRecue::query).containsExactly(tuple("/api/reporting/global", null));
        }
    }

    @Test
    void reporting_global_sans_lot_ou_sans_pays_joignable_renvoie_des_zeros() {
        BRESIL.couper();
        COLOMBIE.repond("GET", "/api/reporting/global", 500, "{}");

        Reponse reponse = get("/reporting/global", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(json("{'avgAgeDays':0.0,'complianceRate':0.0,'countMouvementLast30Days':0,'totalLots':0}"));
    }

    // ---------------------------------------------------------------- qualité

    @Test
    void reporting_qualite_somme_les_compteurs_et_moyenne_les_taux_non_nuls() {
        BRESIL.get("/api/reporting/quality", json("{'alertCount':2,'complianceRate':80.0,'nonComplianceCount':1}"));
        COLOMBIE.get("/api/reporting/quality", json("{'alertCount':3,'complianceRate':60.0,'nonComplianceCount':2}"));

        Reponse reponse = get("/reporting/quality?" + PERIODE, jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(json("{'alertCount':5,'complianceRate':70.0,'nonComplianceCount':3}"));
        for (var pays : TOUS_LES_PAYS) {
            assertThat(pays.requetes()).extracting(RequestRecue::chemin, RequestRecue::query)
                    .containsExactly(tuple("/api/reporting/quality", PERIODE_RELAYEE));
        }
    }

    @Test
    void reporting_qualite_d_un_pays_n_interroge_que_ce_pays() {
        BRESIL.get("/api/reporting/quality", json("{'alertCount':2,'complianceRate':80.0,'nonComplianceCount':1}"));
        COLOMBIE.get("/api/reporting/quality", json("{'alertCount':3,'complianceRate':60.0,'nonComplianceCount':2}"));

        Reponse reponse = get("/reporting/quality?pays=%20Bresil&" + PERIODE, jetonAdmin());

        assertThat(reponse.corps()).isEqualTo(json("{'alertCount':2,'complianceRate':80.0,'nonComplianceCount':1}"));
        assertThat(COLOMBIE.requetes()).isEmpty();
        assertThat(EQUATEUR.requetes()).isEmpty();
    }

    @Test
    void reporting_qualite_ignore_un_pays_en_erreur_ou_injoignable() {
        BRESIL.get("/api/reporting/quality", json("{'alertCount':2,'complianceRate':80.0,'nonComplianceCount':1}"));
        COLOMBIE.couper();
        EQUATEUR.repond("GET", "/api/reporting/quality", 500, "{}");

        Reponse reponse = get("/reporting/quality?" + PERIODE, jetonAdmin());

        assertThat(reponse.corps()).isEqualTo(json("{'alertCount':2,'complianceRate':80.0,'nonComplianceCount':1}"));
    }

    @Test
    void reporting_qualite_d_un_pays_unique_en_erreur_renvoie_des_zeros() {
        BRESIL.couper();

        Reponse reponse = get("/reporting/quality?pays=bresil&" + PERIODE, jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(json("{'alertCount':0,'complianceRate':0.0,'nonComplianceCount':0}"));
    }

    @Test
    void reporting_qualite_avec_un_pays_inconnu_ou_sans_periode_est_refuse_en_403() {
        String jeton = jetonAdmin();

        assertThat(get("/reporting/quality?pays=atlantide&" + PERIODE, jeton).status()).isEqualTo(403);
        assertThat(get("/reporting/quality", jeton).status()).isEqualTo(403);
        assertThat(get("/reporting/quality?startDate=2026-01-01T00:00:00", jeton).status()).isEqualTo(403);
        assertThat(get("/reporting/quality?startDate=hier&endDate=demain", jeton).status()).isEqualTo(403);
    }

    // ---------------------------------------------------------------- stock

    @Test
    void reporting_stock_est_celui_du_pays_demande() {
        String urgent = "f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1";
        String stock = json("{'totalLots':3,'avgAgeDays':12.5,'complianceRate':66.7,'lotIdsUrgentFifo':['%s']}").formatted(urgent);
        BRESIL.get("/api/reporting/stock", stock);

        Reponse reponse = get("/reporting/stock?pays=Bresil&" + PERIODE, jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(stock);
        assertThat(BRESIL.requetes()).extracting(RequestRecue::chemin, RequestRecue::query)
                .containsExactly(tuple("/api/reporting/stock", PERIODE_RELAYEE));
        assertThat(COLOMBIE.requetes()).isEmpty();
    }

    @Test
    void reporting_stock_d_un_pays_en_erreur_renvoie_des_zeros_sans_liste_de_lots_urgents() {
        BRESIL.couper();
        COLOMBIE.repond("GET", "/api/reporting/stock", 404, "{}");
        String jeton = jetonAdmin();

        for (String pays : new String[]{"bresil", "colombie"}) {
            Reponse reponse = get("/reporting/stock?pays=" + pays + "&" + PERIODE, jeton);

            assertThat(reponse.status()).as(pays).isEqualTo(200);
            assertThat(reponse.corps()).as(pays).isEqualTo(json("{'totalLots':0,'avgAgeDays':0.0,'complianceRate':0.0,'lotIdsUrgentFifo':null}"));
        }
    }

    @Test
    void reporting_stock_sans_pays_avec_un_pays_inconnu_ou_sans_periode_est_refuse_en_403() {
        String jeton = jetonAdmin();

        assertThat(get("/reporting/stock?" + PERIODE, jeton).status()).as("sans pays").isEqualTo(403);
        assertThat(get("/reporting/stock?pays=&" + PERIODE, jeton).status()).as("pays vide").isEqualTo(403);
        assertThat(get("/reporting/stock?pays=atlantide&" + PERIODE, jeton).status()).as("pays inconnu").isEqualTo(403);
        assertThat(get("/reporting/stock?pays=bresil", jeton).status()).as("sans période").isEqualTo(403);
    }
}
