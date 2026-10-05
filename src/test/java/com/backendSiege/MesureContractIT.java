package com.backendSiege;

import com.backendSiege.support.FakePaysServer.RequestRecue;
import com.backendSiege.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static com.backendSiege.support.Donnees.*;
import static com.backendSiege.support.Json.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

/** Contrat des mesures des capteurs : une mesure, les mesures d'une zone, la dernière mesure, le résumé d'un entrepôt. */
class MesureContractIT extends IntegrationTestBase {

    // ---------------------------------------------------------------- une mesure

    @Test
    void mesure_est_relayee_depuis_le_pays_demande() {
        BRESIL.get("/api/mesures/getMesure/" + ID_MESURE, mesure(ID_MESURE));

        Reponse reponse = get("/api/mesures/" + ID_MESURE + "?pays=bresil", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(mesure(ID_MESURE));
        assertThat(BRESIL.requetes()).extracting(RequestRecue::methode, RequestRecue::chemin)
                .containsExactly(tuple("GET", "/api/mesures/getMesure/" + ID_MESURE));
    }

    @Test
    void mesure_introuvable_inconnue_ou_injoignable_renvoie_200_sans_corps() {
        BRESIL.get("/api/mesures/getMesure/" + ID_MESURE, "");
        COLOMBIE.couper();
        String jeton = jetonAdmin();

        for (String pays : new String[]{"bresil", "colombie", "equateur", "atlantide"}) {
            Reponse reponse = get("/api/mesures/" + ID_MESURE + "?pays=" + pays, jeton);

            assertThat(reponse.status()).as(pays).isEqualTo(200);
            assertThat(reponse.corps()).as(pays).isEmpty();
        }
    }

    // ---------------------------------------------------------------- mesures d'une zone

    @Test
    void mesures_d_une_zone_sur_une_periode_sont_relayees_avec_les_dates_au_format_du_pays() {
        String mesures = "[" + mesure(ID_MESURE) + "]";
        BRESIL.get("/api/zone/readings/" + ID_ZONE, mesures);

        Reponse reponse = get("/readings/" + ID_ZONE + "/?pays=bresil&startDate=2026-01-01T10:00:30&endDate=2026-01-02T00:00", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(mesures);
        assertThat(BRESIL.requetes()).extracting(RequestRecue::chemin, RequestRecue::query)
                .containsExactly(tuple("/api/zone/readings/" + ID_ZONE, "startDate=2026-01-01T10:00:30&endDate=2026-01-02T00:00"));
    }

    @Test
    void mesures_d_une_zone_d_un_pays_inconnu_en_erreur_ou_injoignable_sont_une_liste_vide() {
        COLOMBIE.repond("GET", "/api/zone/readings/" + ID_ZONE, 500, "{}");
        EQUATEUR.couper();
        String jeton = jetonAdmin();

        for (String pays : new String[]{"atlantide", "bresil", "colombie", "equateur"}) {
            Reponse reponse = get("/readings/" + ID_ZONE + "/?pays=" + pays + "&" + PERIODE, jeton);

            assertThat(reponse.status()).as(pays).isEqualTo(200);
            assertThat(reponse.corps()).as(pays).isEqualTo("[]");
        }
    }

    @Test
    void mesures_d_une_zone_exigent_pays_et_dates_et_le_chemin_se_termine_par_une_barre_oblique() {
        String jeton = jetonAdmin();

        assertThat(get("/readings/" + ID_ZONE + "/?" + PERIODE, jeton).status()).as("sans pays").isEqualTo(403);
        assertThat(get("/readings/" + ID_ZONE + "/?pays=bresil&startDate=2026-01-01T00:00", jeton).status()).as("sans fin").isEqualTo(403);
        assertThat(get("/readings/" + ID_ZONE + "?pays=bresil&" + PERIODE, jeton).status()).as("sans barre finale").isEqualTo(403);
    }

    // ---------------------------------------------------------------- dernière mesure d'une zone

    @Test
    void derniere_mesure_d_une_zone_est_relayee() {
        BRESIL.get("/api/zone/" + ID_ZONE + "/readings/latest", mesure(ID_MESURE));

        Reponse reponse = get("/zones/" + ID_ZONE + "/readings/latest?pays=bresil", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(mesure(ID_MESURE));
    }

    @Test
    void derniere_mesure_absente_inconnue_ou_injoignable_renvoie_200_sans_corps() {
        BRESIL.couper();
        String jeton = jetonAdmin();

        for (String pays : new String[]{"atlantide", "bresil", "colombie"}) {
            Reponse reponse = get("/zones/" + ID_ZONE + "/readings/latest?pays=" + pays, jeton);

            assertThat(reponse.status()).as(pays).isEqualTo(200);
            assertThat(reponse.corps()).as(pays).isEmpty();
        }
    }

    // ---------------------------------------------------------------- résumé des capteurs d'un entrepôt

    @Test
    void resume_des_capteurs_est_renvoye_en_snake_case_avec_les_decimales_du_pays() {
        BRESIL.get("/api/entrepots/" + ID_ENTREPOT + "/capteurSummary", json(
                "[{'zoneId':'%s','zoneName':'A','lastTemp':27.50,'lastHumidity':56.20,'avgTemp24h':27.125,'avgHumidity24h':56}]")
                .formatted(ID_ZONE));

        Reponse reponse = get("/warehouses/" + ID_ENTREPOT + "/readings/summary?pays=bresil", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(json(
                "[{'zone_id':'%s','zone_name':'A','last_temp':27.50,'last_humidity':56.20,'avg_temp_24h':27.125,'avg_humidity_24h':56}]")
                .formatted(ID_ZONE));
    }

    @Test
    void resume_des_capteurs_d_un_pays_inconnu_en_erreur_ou_injoignable_est_une_liste_vide() {
        COLOMBIE.repond("GET", "/api/entrepots/" + ID_ENTREPOT + "/capteurSummary", 500, "{}");
        EQUATEUR.couper();
        String jeton = jetonAdmin();

        for (String pays : new String[]{"atlantide", "colombie", "equateur"}) {
            Reponse reponse = get("/warehouses/" + ID_ENTREPOT + "/readings/summary?pays=" + pays, jeton);

            assertThat(reponse.status()).as(pays).isEqualTo(200);
            assertThat(reponse.corps()).as(pays).isEqualTo("[]");
        }
    }
}
