package com.backendSiege;

import com.backendSiege.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static com.backendSiege.support.Donnees.*;
import static com.backendSiege.support.Json.json;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Le siège relaie ce que répondent les pays sans le contraindre : champs inconnus ignorés, champs absents à {@code null},
 * nombres décimaux acceptés pour des entiers, charge illisible traitée comme une absence de réponse.
 */
class ToleranceContractIT extends IntegrationTestBase {

    private static final String LOT = lot(ID_LOT, "2026-01-10T08:30:00");

    @Test
    void les_champs_inconnus_d_un_pays_sont_ignores() {
        BRESIL.get("/api/lots/getLot/" + ID_LOT, LOT.replace("{\"idLot\"", "{\"nouveau\":1,\"idLot\"")
                .replace("\"detail\":\"Transfert\"", "\"detail\":\"Transfert\",\"autre\":[1]"));
        BRESIL.get("/api/entrepots", json("[{'idEntrepot':'%s','localisation':'S','nom':'N','extra':1}]").formatted(ID_ENTREPOT));
        BRESIL.get("/api/reporting/global", json("{'totalLots':2,'avgAgeDays':10.5,'complianceRate':50,'countMouvementLast30Days':1,'x':3}"));
        String jeton = jetonAdmin();

        assertThat(get("/lot/" + ID_LOT, jeton).corps()).isEqualTo(LOT);
        assertThat(get("/warehouses/bresil", jeton).corps())
                .isEqualTo(json("[{'idEntrepot':'%s','localisation':'S','nom':'N'}]").formatted(ID_ENTREPOT));
        assertThat(get("/reporting/global", jeton).corps())
                .isEqualTo(json("{'avgAgeDays':10.5,'complianceRate':50.0,'countMouvementLast30Days':1,'totalLots':2}"));
    }

    @Test
    void les_champs_absents_sont_renvoyes_a_null_sans_valeur_par_defaut() {
        BRESIL.get("/api/lots/getLot/" + ID_LOT, "{}");
        BRESIL.get("/api/entrepots", json("[{'idEntrepot':'%s'}]").formatted(ID_ENTREPOT));
        BRESIL.get("/api/alertes", "[{\"idAlerte\":\"x\"}]");
        BRESIL.get("/api/entrepots/" + ID_ENTREPOT + "/zones", json("[{'idZone':'%s'}]").formatted(ID_ZONE));
        String jeton = jetonAdmin();

        assertThat(get("/lot/" + ID_LOT, jeton).corps()).isEqualTo(json(
                "{'idLot':null,'status':null,'quantite':null,'caracteristique':null,'dateSortie':null,'dateStockage':null,"
                        + "'dateProduction':null,'emplacement':null,'idExploitation':null,'idEntrepot':null,'historique':null}"));
        assertThat(get("/warehouses/bresil", jeton).corps())
                .isEqualTo(json("[{'idEntrepot':'%s','localisation':null,'nom':null}]").formatted(ID_ENTREPOT));
        assertThat(get("/alertes?idEntrepot=" + ID_ENTREPOT, jeton).corps())
                .isEqualTo(json("[{'idAlerte':'x','typeAlerte':null,'description':null,'dateEmission':null,'idZone':null}]"));
        assertThat(get("/warehouses/" + ID_ENTREPOT + "/zones?pays=bresil", jeton).corps())
                .isEqualTo(json("[{'idZone':'%s','nomZone':null,'idEntrepot':null,'idCapteur':null,'lotCount':null}]").formatted(ID_ZONE));
    }

    @Test
    void un_nombre_decimal_est_accepte_pour_un_entier_et_les_fractions_de_secondes_sont_conservees() {
        BRESIL.get("/api/lots/getLot/" + ID_LOT, lot(ID_LOT, "2026-01-10T08:30:00.123456").replace("\"quantite\":120", "\"quantite\":120.7"));
        BRESIL.get("/api/lots/getAllQuantity", "5.5");

        Reponse lot = get("/lot/" + ID_LOT, jetonAdmin());

        assertThat(lot.corps()).isEqualTo(lot(ID_LOT, "2026-01-10T08:30:00.123456"));
        assertThat(get("/api/lot/total", jetonAdmin()).corps()).isEqualTo("5");
    }

    @Test
    void le_type_d_historique_et_l_emplacement_d_un_lot_sont_relayes_tels_quels() {
        BRESIL.get("/api/lots/getLot/" + ID_LOT, LOT.replace("\"TRANSFERT\"", "\"AUTRE_TYPE\"")
                .replace("\"emplacement\":\"" + ID_ZONE + "\"", "\"emplacement\":\"zone-A\""));

        Reponse reponse = get("/lot/" + ID_LOT, jetonAdmin());

        assertThat(reponse.status()).isEqualTo(200);
        assertThat(reponse.corps()).contains("\"type\":\"AUTRE_TYPE\"").contains("\"emplacement\":\"zone-A\"");
    }

    @Test
    void une_charge_illisible_ou_vide_est_traitee_comme_une_absence_de_reponse() {
        BRESIL.get("/api/lots/getLot/" + ID_LOT, "pas du json");
        COLOMBIE.get("/api/lots/getLot/" + ID_LOT, "");
        BRESIL.get("/api/lots/getAllQuantity", "pas un entier");
        String jeton = jetonAdmin();

        assertThat(get("/lot/" + ID_LOT, jeton).status()).isEqualTo(404);
        assertThat(get("/api/lot/total", jeton).status()).isEqualTo(404);
    }

    @Test
    void les_quantites_negatives_sont_additionnees_telles_quelles() {
        BRESIL.get("/api/lots/getAllQuantity", "7");
        COLOMBIE.get("/api/lots/getAllQuantity", "-3");

        assertThat(get("/api/lot/total", jetonAdmin()).corps()).isEqualTo("4");
    }
}
