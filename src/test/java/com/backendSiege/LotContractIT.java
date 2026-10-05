package com.backendSiege;

import com.backendSiege.support.FakePaysServer.RequestRecue;
import com.backendSiege.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.backendSiege.support.Donnees.*;
import static com.backendSiege.support.Json.equivalents;
import static com.backendSiege.support.Json.json;
import static org.assertj.core.api.Assertions.assertThat;

/** Contrat des routes « lot » : inventaire, mouvements de stock, mesures d'un lot. */
class LotContractIT extends IntegrationTestBase {

    private static final String LOT_BRESIL = lot(ID_LOT, "2026-01-10T08:30:00");
    private static final String LOT_COLOMBIE = lot(ID_LOT_COLOMBIE, "2026-01-05T08:30:00");
    private static final String LOT_SANS_DATE = lot(java.util.UUID.fromString("77777777-7777-7777-7777-777777777777"), null);

    // ---------------------------------------------------------------- GET /lots

    @Test
    void lots_agregue_les_pays_du_plus_ancien_au_plus_recent_les_lots_sans_date_en_dernier() {
        BRESIL.get("/api/lots/allLotsFromAllEntrepots", "[" + LOT_SANS_DATE + "," + LOT_BRESIL + "]");
        COLOMBIE.get("/api/lots/allLotsFromAllEntrepots", "[" + LOT_COLOMBIE + "]");

        Reponse reponse = get("/lots", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.entetes().firstValue("Content-Type")).hasValueSatisfying(t -> assertThat(t).startsWith("application/json"));
        assertThat(reponse.corps()).isEqualTo("[" + LOT_COLOMBIE + "," + LOT_BRESIL + "," + LOT_SANS_DATE + "]");
    }

    @Test
    void lots_ignore_un_pays_qui_repond_en_erreur_ou_qui_est_injoignable() {
        BRESIL.get("/api/lots/allLotsFromAllEntrepots", "[" + LOT_BRESIL + "]");
        COLOMBIE.repond("GET", "/api/lots/allLotsFromAllEntrepots", 500, "{}");
        EQUATEUR.couper();

        Reponse reponse = get("/lots", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo("[" + LOT_BRESIL + "]");
    }

    @Test
    void lots_filtre_par_pays_en_ignorant_la_casse_et_n_appelle_que_ce_pays() {
        BRESIL.get("/api/lots/allLotsFromAllEntrepots", "[" + LOT_BRESIL + "]");
        COLOMBIE.get("/api/lots/allLotsFromAllEntrepots", "[" + LOT_COLOMBIE + "]");

        Reponse reponse = get("/lots?pays=bResil", jetonAdmin());

        assertThat(reponse.corps()).isEqualTo("[" + LOT_BRESIL + "]");
        assertThat(COLOMBIE.requetes()).isEmpty();
        assertThat(EQUATEUR.requetes()).isEmpty();
    }

    @Test
    void lots_avec_un_pays_inconnu_interroge_tous_les_pays() {
        BRESIL.get("/api/lots/allLotsFromAllEntrepots", "[" + LOT_BRESIL + "]");
        COLOMBIE.get("/api/lots/allLotsFromAllEntrepots", "[" + LOT_COLOMBIE + "]");

        Reponse reponse = get("/lots?pays=atlantide", jetonAdmin());

        assertThat(reponse.corps()).isEqualTo("[" + LOT_COLOMBIE + "," + LOT_BRESIL + "]");
    }

    @Test
    void lots_sans_aucun_lot_renvoie_une_liste_vide() {
        Reponse reponse = get("/lots", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo("[]");
    }

    // ---------------------------------------------------------------- GET /lot/{id}

    @Test
    void lot_renvoie_le_lot_du_pays_qui_le_possede() {
        COLOMBIE.get("/api/lots/getLot/" + ID_LOT, LOT_BRESIL);

        Reponse reponse = get("/lot/" + ID_LOT, jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(LOT_BRESIL);
        assertThat(BRESIL.requetes()).extracting(RequestRecue::chemin).containsExactly("/api/lots/getLot/" + ID_LOT);
        assertThat(EQUATEUR.requetes()).extracting(RequestRecue::chemin).containsExactly("/api/lots/getLot/" + ID_LOT);
    }

    @Test
    void lot_inconnu_de_tous_les_pays_renvoie_404_sans_corps() {
        Reponse reponse = get("/lot/" + ID_LOT, jetonAdmin());

        assertThat(reponse.status()).isEqualTo(404);
        assertThat(reponse.corps()).isEmpty();
    }

    @Test
    void lot_est_trouve_meme_si_un_autre_pays_est_injoignable() {
        BRESIL.couper();
        EQUATEUR.repond("GET", "/api/lots/getLot/" + ID_LOT, 500, "{}");
        COLOMBIE.get("/api/lots/getLot/" + ID_LOT, LOT_BRESIL);

        Reponse reponse = get("/lot/" + ID_LOT, jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(LOT_BRESIL);
    }

    // ---------------------------------------------------------------- GET /lot/{id}/mesures

    @Test
    void mesures_d_un_lot_sont_celles_du_pays_qui_le_possede() {
        String mesures = json("[{'temperature':27.5,'humidite':56.2,'dateMesure':'2026-01-12T10:00:00'},"
                + "{'temperature':28.0,'humidite':null,'dateMesure':'2026-01-12T11:00:00'}]");
        COLOMBIE.get("/api/lots/getAllMesuresByLotId/" + ID_LOT, mesures);

        Reponse reponse = get("/lot/" + ID_LOT + "/mesures", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(mesures);
    }

    @Test
    void mesures_d_un_lot_inconnu_renvoie_une_liste_vide_en_200() {
        Reponse reponse = get("/lot/" + ID_LOT + "/mesures", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo("[]");
    }

    // ---------------------------------------------------------------- GET /lot/oldest

    @Test
    void lot_le_plus_ancien_est_choisi_parmi_tous_les_pays() {
        BRESIL.get("/api/lots/oldestLot", LOT_BRESIL);
        COLOMBIE.get("/api/lots/oldestLot", LOT_COLOMBIE);

        Reponse reponse = get("/lot/oldest", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(LOT_COLOMBIE);
    }

    @Test
    void lot_le_plus_ancien_d_un_pays_n_interroge_que_ce_pays() {
        BRESIL.get("/api/lots/oldestLot", LOT_BRESIL);
        COLOMBIE.get("/api/lots/oldestLot", LOT_COLOMBIE);

        Reponse reponse = get("/lot/oldest?pays=%20Bresil%20", jetonAdmin());

        assertThat(reponse.corps()).isEqualTo(LOT_BRESIL);
        assertThat(COLOMBIE.requetes()).isEmpty();
    }

    @Test
    void lot_le_plus_ancien_renvoie_404_pour_un_pays_inconnu_ou_sans_lot() {
        BRESIL.get("/api/lots/oldestLot", LOT_BRESIL);

        Reponse inconnu = get("/lot/oldest?pays=atlantide", jetonAdmin());
        Reponse sansLot = get("/lot/oldest?pays=equateur", jetonAdmin());

        assertThat(inconnu.status()).isEqualTo(404);
        assertThat(inconnu.corps()).isEmpty();
        assertThat(sansLot.status()).isEqualTo(404);
        assertThat(sansLot.corps()).isEmpty();
    }

    @Test
    void lot_le_plus_ancien_ignore_un_pays_injoignable() {
        BRESIL.couper();
        COLOMBIE.get("/api/lots/oldestLot", LOT_COLOMBIE);

        Reponse reponse = get("/lot/oldest", jetonAdmin());

        assertThat(reponse.corps()).isEqualTo(LOT_COLOMBIE);
    }

    // ---------------------------------------------------------------- GET /api/lot/total

    @Test
    void quantite_totale_additionne_les_pays() {
        BRESIL.get("/api/lots/getAllQuantity", "100");
        COLOMBIE.get("/api/lots/getAllQuantity", "50");

        Reponse reponse = get("/api/lot/total", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo("150");
    }

    @Test
    void quantite_totale_compte_zero_pour_un_pays_en_erreur_ou_injoignable() {
        BRESIL.get("/api/lots/getAllQuantity", "100");
        COLOMBIE.repond("GET", "/api/lots/getAllQuantity", 500, "{}");
        EQUATEUR.couper();

        Reponse reponse = get("/api/lot/total", jetonAdmin());

        assertThat(reponse.corps()).isEqualTo("100");
    }

    @Test
    void quantite_totale_nulle_renvoie_404_sans_corps() {
        Reponse reponse = get("/api/lot/total", jetonAdmin());

        assertThat(reponse.status()).isEqualTo(404);
        assertThat(reponse.corps()).isEmpty();
    }

    // ---------------------------------------------------------------- POST /lot

    @Test
    void creation_d_un_lot_est_relayee_au_pays_et_son_resultat_renvoye() {
        COLOMBIE.repond("POST", "/api/lots", 201, json("{'idLot':'%s','status':'CREATED'}").formatted(ID_LOT));
        String requete = json("{'status':'COMPLIANT','quantite':120,'caracteristique':2,'dateProduction':'2026-03-15',"
                + "'idExploitation':'%s','idZone':'%s','inconnu':true}").formatted(ID_EXPLOITATION, ID_ZONE);

        Reponse reponse = post("/lot?pays=colombie", jetonAdmin(), requete);

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(json("{'idLot':'%s','status':'CREATED'}").formatted(ID_LOT));
        List<RequestRecue> recues = COLOMBIE.requetes("/api/lots");
        assertThat(recues).hasSize(1);
        assertThat(recues.get(0).methode()).isEqualTo("POST");
        assertThat(equivalents(recues.get(0).corps(), json("{'status':'COMPLIANT','quantite':120,'caracteristique':2,"
                + "'dateProduction':'2026-03-15','idExploitation':'%s','idZone':'%s'}").formatted(ID_EXPLOITATION, ID_ZONE))).isTrue();
        assertThat(BRESIL.requetes()).isEmpty();
    }

    @Test
    void creation_d_un_lot_transmet_les_champs_absents_a_null() {
        BRESIL.repond("POST", "/api/lots", 201, json("{'idLot':'%s','status':'CREATED'}").formatted(ID_LOT));

        post("/lot?pays=bresil", jetonAdmin(), "{}");

        assertThat(equivalents(BRESIL.requetes("/api/lots").get(0).corps(), json(
                "{'status':null,'quantite':null,'caracteristique':null,'dateProduction':null,'idExploitation':null,'idZone':null}")))
                .isTrue();
    }

    @Test
    void creation_d_un_lot_renvoie_200_sans_corps_si_le_pays_est_inconnu_refuse_ou_injoignable() {
        String jeton = jetonAdmin();
        BRESIL.repond("POST", "/api/lots", 500, "{}");
        COLOMBIE.couper();

        for (String pays : new String[]{"atlantide", "bresil", "colombie", "equateur"}) {
            Reponse reponse = post("/lot?pays=" + pays, jeton, json("{'status':'COMPLIANT'}"));

            assertThat(reponse.status()).as(pays).isEqualTo(200);
            assertThat(reponse.corps()).as(pays).isEmpty();
        }
    }

    @Test
    void creation_d_un_lot_sans_pays_est_refusee_en_403() {
        assertThat(post("/lot", jetonAdmin(), "{}").status()).isEqualTo(403);
    }

    // ---------------------------------------------------------------- PUT /lot/{id}/status

    @Test
    void changement_de_statut_est_relaye_au_pays() {
        EQUATEUR.repond("PUT", "/api/lots/" + ID_LOT + "/status", 200, json("{'idLot':'%s','status':'OK'}").formatted(ID_LOT));

        Reponse reponse = put("/lot/" + ID_LOT + "/status?pays=equateur", jetonAdmin(), json("{'status':'BLOCKED','extra':1}"));

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(json("{'idLot':'%s','status':'OK'}").formatted(ID_LOT));
        assertThat(EQUATEUR.requetes("/api/lots/" + ID_LOT + "/status")).extracting(RequestRecue::methode, RequestRecue::corps)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("PUT", json("{'status':'BLOCKED'}")));
    }

    @Test
    void changement_de_statut_sans_statut_transmet_un_statut_nul() {
        BRESIL.repond("PUT", "/api/lots/" + ID_LOT + "/status", 200, json("{'idLot':'%s','status':'OK'}").formatted(ID_LOT));

        Reponse reponse = put("/lot/" + ID_LOT + "/status?pays=bresil", jetonAdmin(), "{}");

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(equivalents(BRESIL.requetes("/api/lots/" + ID_LOT + "/status").get(0).corps(), "{\"status\":null}")).isTrue();
    }

    @Test
    void changement_de_statut_renvoie_200_sans_corps_si_le_pays_est_inconnu_ou_ne_trouve_pas_le_lot() {
        BRESIL.repond("PUT", "/api/lots/" + ID_LOT + "/status", 404, json("{'idLot':null,'status':'NOT_FOUND'}"));
        String jeton = jetonAdmin();

        Reponse introuvable = put("/lot/" + ID_LOT + "/status?pays=bresil", jeton, json("{'status':'BLOCKED'}"));
        Reponse paysInconnu = put("/lot/" + ID_LOT + "/status?pays=atlantide", jeton, json("{'status':'BLOCKED'}"));
        COLOMBIE.couper();
        Reponse injoignable = put("/lot/" + ID_LOT + "/status?pays=colombie", jeton, json("{'status':'BLOCKED'}"));

        for (Reponse reponse : List.of(introuvable, paysInconnu, injoignable)) {
            assertThat(reponse.status()).isEqualTo(200);
            assertThat(reponse.corps()).isEmpty();
        }
    }

    // ---------------------------------------------------------------- PUT /lot/{id}/zones

    @Test
    void transfert_de_zone_est_relaye_au_pays() {
        BRESIL.repond("PUT", "/api/lots/" + ID_LOT + "/zone", 200, json("{'idLot':'%s','status':'OK'}").formatted(ID_LOT));

        Reponse reponse = put("/lot/" + ID_LOT + "/zones?pays=bresil", jetonAdmin(), json("{'idZone':'%s'}").formatted(ID_ZONE));

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(json("{'idLot':'%s','status':'OK'}").formatted(ID_LOT));
        assertThat(BRESIL.requetes("/api/lots/" + ID_LOT + "/zone")).extracting(RequestRecue::methode, RequestRecue::corps)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("PUT", json("{'idZone':'%s'}").formatted(ID_ZONE)));
    }

    @Test
    void transfert_de_zone_est_refuse_en_403_si_le_pays_repond_en_erreur_ou_est_injoignable() {
        String jeton = jetonAdmin();
        String corps = json("{'idZone':'%s'}").formatted(ID_ZONE);
        BRESIL.repond("PUT", "/api/lots/" + ID_LOT + "/zone", 400, "{}");
        COLOMBIE.repond("PUT", "/api/lots/" + ID_LOT + "/zone", 404, json("{'idLot':null,'status':'NOT_FOUND'}"));
        EQUATEUR.couper();

        for (String pays : new String[]{"bresil", "colombie", "equateur"}) {
            Reponse reponse = put("/lot/" + ID_LOT + "/zones?pays=" + pays, jeton, corps);

            assertThat(reponse.status()).as(pays).isEqualTo(403);
            assertThat(reponse.corps()).as(pays).isEmpty();
        }
    }

    @Test
    void transfert_de_zone_renvoie_200_sans_corps_pour_un_pays_inconnu() {
        Reponse reponse = put("/lot/" + ID_LOT + "/zones?pays=atlantide", jetonAdmin(), json("{'idZone':'%s'}").formatted(ID_ZONE));

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEmpty();
    }

    // ---------------------------------------------------------------- POST /mouvements/stockout

    @Test
    void sortie_de_stock_est_relayee_au_pays() {
        BRESIL.repond("POST", "/api/lots/stock-out", 201, json("{'idHistorique':'%s','status':'CREATED'}").formatted(ID_HISTORIQUE));

        Reponse reponse = post("/mouvements/stockout?idLot=" + ID_LOT + "&pays=bresil", jetonAdmin(), null);

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).isEqualTo(json("{'idHistorique':'%s','status':'CREATED'}").formatted(ID_HISTORIQUE));
        assertThat(BRESIL.requetes("/api/lots/stock-out")).extracting(RequestRecue::methode, RequestRecue::query, RequestRecue::corps)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("POST", "lotId=" + ID_LOT, ""));
    }

    @Test
    void sortie_de_stock_est_refusee_en_403_si_le_pays_repond_en_erreur_et_sans_corps_pour_un_pays_inconnu() {
        String jeton = jetonAdmin();
        BRESIL.repond("POST", "/api/lots/stock-out", 404, "{}");

        Reponse enErreur = post("/mouvements/stockout?idLot=" + ID_LOT + "&pays=bresil", jeton, null);
        Reponse paysInconnu = post("/mouvements/stockout?idLot=" + ID_LOT + "&pays=atlantide", jeton, null);
        Reponse sansLot = post("/mouvements/stockout?pays=bresil", jeton, null);

        assertThat(enErreur.status()).isEqualTo(403);
        assertThat(paysInconnu.status()).isEqualTo(200);
        assertThat(paysInconnu.corps()).isEmpty();
        assertThat(sansLot.status()).isEqualTo(403);
    }
}
