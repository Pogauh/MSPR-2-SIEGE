package com.backendSiege;

import com.backendSiege.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

import static com.backendSiege.support.Donnees.ID_LOT;
import static com.backendSiege.support.Json.lire;
import static org.assertj.core.api.Assertions.assertThat;

/** Surface de l'API : routes exposées (vues par la documentation OpenAPI) et erreurs communes à toutes les routes. */
class RoutesContractIT extends IntegrationTestBase {

    @Test
    void les_routes_exposees_sont_celles_attendues_par_le_front() {
        JsonNode chemins = lire(get("/v3/api-docs", null).corps()).get("paths");

        List<String> routes = new ArrayList<>();
        chemins.propertyNames().forEach(chemin ->
                chemins.get(chemin).propertyNames().forEach(methode -> routes.add(methode.toUpperCase() + " " + chemin)));

        assertThat(routes).containsExactlyInAnyOrder(
                "POST /api/auth/register",
                "POST /api/auth/login",
                "GET /api/auth/me",
                "GET /countries",
                "GET /farms/{pays}",
                "GET /warehouses/{pays}",
                "GET /warehouses/{entrepotId}/zones",
                "GET /warehouses/{idEntrepot}/readings/summary",
                "GET /readings/{idZone}/",
                "GET /zones/{idZone}/readings/latest",
                "GET /api/mesures/{mesureId}",
                "GET /api/lot/total",
                "GET /lots",
                "GET /lot/{id}",
                "GET /lot/{idLot}/mesures",
                "GET /lot/oldest",
                "POST /lot",
                "PUT /lot/{idLot}/status",
                "PUT /lot/{idLot}/zones",
                "POST /mouvements/stockout",
                "GET /history/{idLot}",
                "GET /alertes",
                "GET /reporting/global",
                "GET /reporting/quality",
                "GET /reporting/stock");
    }

    /**
     * Comportement historique : Spring Security refuse la redirection interne vers {@code /error} à un contexte anonyme,
     * si bien que toute erreur non traitée par un contrôleur (route inconnue, paramètre invalide, exception) sort en 403 sans corps.
     */
    @Test
    void les_erreurs_non_traitees_par_un_controleur_sortent_en_403_sans_corps() {
        String jeton = jetonAdmin();

        Reponse routeInconnue = get("/n-existe-pas", jeton);
        Reponse uuidInvalide = get("/lot/pas-un-uuid", jeton);
        Reponse methodeInterdite = post("/lots", jeton, "{}");
        Reponse corpsIllisible = post("/lot?pays=bresil", jeton, "{pas du json");

        for (Reponse reponse : List.of(routeInconnue, uuidInvalide, methodeInterdite, corpsIllisible)) {
            assertThat(reponse.status()).isEqualTo(403);
            assertThat(reponse.corps()).isEmpty();
        }
    }

    @Test
    void un_lot_est_demande_par_son_identifiant_uuid() {
        assertThat(get("/lot/" + ID_LOT, jetonAdmin()).status()).isEqualTo(404);
    }
}
