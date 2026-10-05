package com.backendSiege;

import com.backendSiege.support.FakePaysServer.RequestRecue;
import com.backendSiege.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static com.backendSiege.support.Donnees.*;
import static com.backendSiege.support.Json.json;
import static org.assertj.core.api.Assertions.assertThat;

/** Contrat du référentiel : pays, exploitations, entrepôts et zones. */
class ReferentielContractIT extends IntegrationTestBase {

    private static final String EXPLOITATIONS = json("[{'idExploitation':'%s','nom':'Ferme du Sud','adresse':'12 rue des Cafiers'}]")
            .formatted(ID_EXPLOITATION);
    private static final String ENTREPOTS = json("[{'idEntrepot':'%s','localisation':'Santos','nom':'Entrepôt principal'}]")
            .formatted(ID_ENTREPOT);

    @Test
    void pays_expose_la_configuration_de_chaque_pays_avec_son_url_et_ses_tolerances() {
        Reponse reponse = get("/countries", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(json("{"
                + "'BRESIL':{'tolerences':{'humiditeIdeale':55,'temperatureIdeale':29,'toleranceHumidite':2,'toleranceTemperature':3},'url':'%s'},"
                + "'COLOMBIE':{'tolerences':{'humiditeIdeale':80,'temperatureIdeale':26,'toleranceHumidite':2,'toleranceTemperature':3},'url':'%s'},"
                + "'EQUATEUR':{'tolerences':{'humiditeIdeale':60,'temperatureIdeale':31,'toleranceHumidite':2,'toleranceTemperature':3},'url':'%s'}}")
                .formatted(BRESIL.baseUrl(), COLOMBIE.baseUrl(), EQUATEUR.baseUrl()));
    }

    // ---------------------------------------------------------------- exploitations

    @Test
    void exploitations_sont_celles_du_pays_demande_sans_tenir_compte_de_la_casse() {
        BRESIL.get("/api/exploitation/allExploitations", EXPLOITATIONS);

        Reponse reponse = get("/farms/BrEsIl", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(EXPLOITATIONS);
        assertThat(COLOMBIE.requetes()).isEmpty();
    }

    @Test
    void exploitations_d_un_pays_inconnu_est_une_liste_vide() {
        Reponse reponse = get("/farms/atlantide", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo("[]");
    }

    @Test
    void exploitations_d_un_pays_injoignable_ou_en_erreur_renvoie_200_sans_corps() {
        BRESIL.couper();
        COLOMBIE.repond("GET", "/api/exploitation/allExploitations", 500, "{}");
        String jeton = jetonAdmin();

        for (String pays : new String[]{"bresil", "colombie"}) {
            Reponse reponse = get("/farms/" + pays, jeton);

            assertThat(reponse.status()).as(pays).isEqualTo(200);
            assertThat(reponse.corps()).as(pays).isEmpty();
        }
    }

    // ---------------------------------------------------------------- entrepôts

    @Test
    void entrepots_sont_ceux_du_pays_demande() {
        COLOMBIE.get("/api/entrepots", ENTREPOTS);

        Reponse reponse = get("/warehouses/colombie", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(ENTREPOTS);
        assertThat(BRESIL.requetes()).isEmpty();
    }

    @Test
    void entrepots_d_un_pays_inconnu_est_une_liste_vide_et_d_un_pays_injoignable_un_corps_vide() {
        EQUATEUR.couper();
        String jeton = jetonAdmin();

        Reponse inconnu = get("/warehouses/atlantide", jeton);
        Reponse injoignable = get("/warehouses/equateur", jeton);

        assertThat(inconnu.corps()).isEqualTo("[]");
        assertThat(injoignable.status()).isEqualTo(200);
        assertThat(injoignable.corps()).isEmpty();
    }

    // ---------------------------------------------------------------- zones

    @Test
    void zones_d_un_entrepot_comptent_les_lots_et_designent_le_capteur() {
        String zones = json("[{'idZone':'%s','nomZone':'A','idEntrepot':'%s','idCapteur':'%s','lotCount':3},"
                + "{'idZone':'%s','nomZone':'B','idEntrepot':'%s','idCapteur':null,'lotCount':0}]")
                .formatted(ID_ZONE, ID_ENTREPOT, ID_CAPTEUR, ID_ZONE_PRECEDENTE, ID_ENTREPOT);
        BRESIL.get("/api/entrepots/" + ID_ENTREPOT + "/zones", zones);

        Reponse reponse = get("/warehouses/" + ID_ENTREPOT + "/zones?pays=bresil", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(zones);
    }

    @Test
    void zones_d_un_pays_inconnu_est_une_liste_vide() {
        Reponse reponse = get("/warehouses/" + ID_ENTREPOT + "/zones?pays=atlantide", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo("[]");
    }

    @Test
    void zones_d_un_pays_injoignable_sont_refusees_en_403() {
        BRESIL.couper();

        Reponse reponse = get("/warehouses/" + ID_ENTREPOT + "/zones?pays=bresil", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(403);
        assertThat(reponse.corps()).isEmpty();
    }

    @Test
    void zones_sans_pays_sont_refusees_en_403() {
        assertThat(get("/warehouses/" + ID_ENTREPOT + "/zones", jetonAdmin()).status()).isEqualTo(403);
    }

    @Test
    void les_appels_au_pays_utilisent_les_chemins_attendus_par_le_backend_pays() {
        BRESIL.get("/api/exploitation/allExploitations", EXPLOITATIONS);
        BRESIL.get("/api/entrepots", ENTREPOTS);
        BRESIL.get("/api/entrepots/" + ID_ENTREPOT + "/zones", "[]");
        String jeton = jetonAdmin();

        get("/farms/bresil", jeton);
        get("/warehouses/bresil", jeton);
        get("/warehouses/" + ID_ENTREPOT + "/zones?pays=bresil", jeton);

        assertThat(BRESIL.requetes()).extracting(RequestRecue::methode, RequestRecue::chemin, RequestRecue::query)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("GET", "/api/exploitation/allExploitations", null),
                        org.assertj.core.groups.Tuple.tuple("GET", "/api/entrepots", null),
                        org.assertj.core.groups.Tuple.tuple("GET", "/api/entrepots/" + ID_ENTREPOT + "/zones", null));
    }
}
