package com.backendSiege;

import com.backendSiege.support.FakePaysServer.RequestRecue;
import com.backendSiege.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static com.backendSiege.support.Donnees.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

/** Contrat de l'historique d'un lot et des alertes d'un entrepôt. */
class HistoriqueAlerteContractIT extends IntegrationTestBase {

    private static final String CHEMIN_HISTORIQUE = "/api/historique/allHistoriquesForLot/" + ID_LOT;

    // ---------------------------------------------------------------- historique

    @Test
    void historique_d_un_lot_est_celui_du_pays_qui_le_possede_et_les_autres_pays_sont_interroges() {
        String historique = "[" + historique("TRANSFERT") + "," + historique("ALERTE") + "]";
        COLOMBIE.get(CHEMIN_HISTORIQUE, historique);

        Reponse reponse = get("/history/" + ID_LOT, jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(historique);
        assertThat(BRESIL.requetes()).extracting(RequestRecue::chemin, RequestRecue::query).containsExactly(tuple(CHEMIN_HISTORIQUE, null));
        assertThat(EQUATEUR.requetes()).extracting(RequestRecue::chemin, RequestRecue::query).containsExactly(tuple(CHEMIN_HISTORIQUE, null));
    }

    @Test
    void historique_filtre_par_type_sans_tenir_compte_de_la_casse_et_accepte_alert_pour_alerte() {
        BRESIL.get(CHEMIN_HISTORIQUE, "[" + historique("ALERTE") + "]");
        String jeton = jetonAdmin();

        assertThat(get("/history/" + ID_LOT + "?type=alert", jeton).corps()).isEqualTo("[" + historique("ALERTE") + "]");
        get("/history/" + ID_LOT + "?type=transfert", jeton);
        get("/history/" + ID_LOT + "?type=%20Shipped%20", jeton);
        get("/history/" + ID_LOT + "?type=", jeton);

        assertThat(BRESIL.requetes()).extracting(RequestRecue::query)
                .containsExactly("type=ALERTE", "type=TRANSFERT", "type=SHIPPED", null);
    }

    @Test
    void historique_avec_un_type_inconnu_est_refuse_en_400_sans_corps() {
        Reponse reponse = get("/history/" + ID_LOT + "?type=inconnu", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(400);
        assertThat(reponse.corps()).isEmpty();
        assertThat(BRESIL.requetes()).isEmpty();
    }

    @Test
    void historique_d_un_lot_inconnu_ou_de_pays_injoignables_est_une_liste_vide() {
        BRESIL.couper();
        COLOMBIE.repond("GET", CHEMIN_HISTORIQUE, 500, "{}");

        Reponse reponse = get("/history/" + ID_LOT, jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo("[]");
    }

    // ---------------------------------------------------------------- alertes

    @Test
    void alertes_d_un_entrepot_interrogent_tous_les_pays_avec_les_dates_au_format_du_pays() {
        BRESIL.get("/api/alertes", "[" + alerte() + "]");

        Reponse reponse = get("/alertes?idEntrepot=" + ID_ENTREPOT + "&startDate=2026-01-01T10:00:30&endDate=2026-01-02T00:00&pays=bresil",
                jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo("[" + alerte() + "]");
        String query = "idEntrepot=" + ID_ENTREPOT + "&startDate=2026-01-01T10:00:30&endDate=2026-01-02T00:00";
        for (var pays : TOUS_LES_PAYS) {
            assertThat(pays.requetes()).extracting(RequestRecue::chemin, RequestRecue::query).containsExactly(tuple("/api/alertes", query));
        }
    }

    @Test
    void alertes_sans_periode_n_envoient_que_l_entrepot_et_le_pays_est_facultatif() {
        COLOMBIE.get("/api/alertes", "[" + alerte() + "]");

        Reponse reponse = get("/alertes?idEntrepot=" + ID_ENTREPOT, jetonAdmin());

        assertThat(reponse.corps()).isEqualTo("[" + alerte() + "]");
        assertThat(BRESIL.requetes()).extracting(RequestRecue::query).containsExactly("idEntrepot=" + ID_ENTREPOT);
    }

    @Test
    void alertes_ignorent_un_pays_en_erreur_ou_injoignable() {
        BRESIL.repond("GET", "/api/alertes", 500, "{}");
        COLOMBIE.couper();
        EQUATEUR.get("/api/alertes", "[" + alerte() + "]");

        Reponse reponse = get("/alertes?idEntrepot=" + ID_ENTREPOT, jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo("[" + alerte() + "]");
    }

    @Test
    void alertes_sans_entrepot_ou_avec_un_identifiant_invalide_sont_refusees_en_403() {
        String jeton = jetonAdmin();

        assertThat(get("/alertes", jeton).status()).isEqualTo(403);
        assertThat(get("/alertes?idEntrepot=pas-un-uuid", jeton).status()).isEqualTo(403);
    }
}
