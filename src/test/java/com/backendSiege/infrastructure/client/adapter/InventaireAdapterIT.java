package com.backendSiege.infrastructure.client.adapter;

import com.backendSiege.domain.exceptions.PaysIndisponibleException;
import com.backendSiege.domain.models.Lot;
import com.backendSiege.domain.models.MesureLot;
import com.backendSiege.domain.models.NouveauLot;
import com.backendSiege.domain.models.ResultatLot;
import com.backendSiege.domain.models.ResultatSortieStock;
import com.backendSiege.infrastructure.client.mapper.PaysMapperImpl;
import com.backendSiege.support.FakePaysServer;
import com.backendSiege.support.FakePaysServer.RequestRecue;
import com.backendSiege.support.PaysTestClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.backendSiege.support.Donnees.ID_ENTREPOT;
import static com.backendSiege.support.Donnees.ID_EXPLOITATION;
import static com.backendSiege.support.Donnees.ID_HISTORIQUE;
import static com.backendSiege.support.Donnees.ID_LOT;
import static com.backendSiege.support.Donnees.ID_LOT_COLOMBIE;
import static com.backendSiege.support.Donnees.ID_ZONE;
import static com.backendSiege.support.Donnees.lot;
import static com.backendSiege.support.Json.equivalents;
import static com.backendSiege.support.Json.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

class InventaireAdapterIT {

    private static final List<String> TOUS = List.of("BRESIL", "COLOMBIE", "EQUATEUR");

    Map<String, FakePaysServer> serveurs;
    FakePaysServer bresil;
    FakePaysServer colombie;
    FakePaysServer equateur;
    InventaireAdapter adapter;

    @BeforeEach
    void demarrer() {
        serveurs = PaysTestClients.troisServeurs();
        bresil = serveurs.get("BRESIL");
        colombie = serveurs.get("COLOMBIE");
        equateur = serveurs.get("EQUATEUR");
        adapter = new InventaireAdapter(PaysTestClients.clients(serveurs), new PaysMapperImpl());
    }

    @AfterEach
    void arreter() {
        serveurs.values().forEach(FakePaysServer::couper);
    }

    // ------------------------------------------------------------------ quantités

    @Test
    void quantites_en_stock_une_par_pays_dans_l_ordre_demande() {
        bresil.get("/api/lots/getAllQuantity", "100");
        colombie.get("/api/lots/getAllQuantity", "50");

        assertThat(adapter.findQuantitesEnStock(TOUS)).containsExactly(100, 50, 0);
        assertThat(adapter.findQuantitesEnStock(List.of("COLOMBIE", "BRESIL"))).containsExactly(50, 100);
    }

    @Test
    void quantites_en_stock_valent_zero_pour_un_pays_en_erreur_injoignable_ou_illisible() {
        bresil.repond("GET", "/api/lots/getAllQuantity", 500, "{}");
        colombie.couper();
        equateur.get("/api/lots/getAllQuantity", "pas un entier");

        assertThat(adapter.findQuantitesEnStock(TOUS)).containsExactly(0, 0, 0);
    }

    @Test
    void quantites_en_stock_ignorent_un_code_de_pays_inconnu() {
        bresil.get("/api/lots/getAllQuantity", "100");

        assertThat(adapter.findQuantitesEnStock(List.of("BRESIL", "ATLANTIDE"))).containsExactly(100);
    }

    // ------------------------------------------------------------------ lecture

    @Test
    void lot_est_celui_du_premier_pays_qui_le_possede_dans_l_ordre_demande() {
        colombie.get("/api/lots/getLot/" + ID_LOT, lot(ID_LOT, "2026-01-10T08:30:00"));
        equateur.get("/api/lots/getLot/" + ID_LOT, lot(ID_LOT, "2026-02-10T08:30:00"));

        Lot lot = adapter.findLot(TOUS, ID_LOT).orElseThrow();

        assertThat(lot.idLot()).isEqualTo(ID_LOT);
        assertThat(lot.dateStockage()).isEqualTo(LocalDateTime.of(2026, 1, 10, 8, 30));
        assertThat(lot.historique()).hasSize(1);
        assertThat(lot.historique().get(0).type()).isEqualTo("TRANSFERT");
    }

    @Test
    void lot_reprend_tous_les_champs_du_pays() {
        bresil.get("/api/lots/getLot/" + ID_LOT, lot(ID_LOT, "2026-01-10T08:30:00"));

        Lot lot = adapter.findLot(List.of("BRESIL"), ID_LOT).orElseThrow();

        assertThat(lot).isEqualTo(new Lot(ID_LOT, "COMPLIANT", 120, 2, null, LocalDateTime.of(2026, 1, 10, 8, 30), LocalDate.of(2025, 12, 1),
                ID_ZONE.toString(), ID_EXPLOITATION, ID_ENTREPOT, lot.historique()));
    }

    @Test
    void lot_est_vide_si_aucun_pays_ne_le_possede_ou_ne_repond() {
        bresil.couper();
        colombie.repond("GET", "/api/lots/getLot/" + ID_LOT, 500, "{}");

        assertThat(adapter.findLot(TOUS, ID_LOT)).isEmpty();
    }

    @Test
    void lots_sont_ceux_de_chaque_pays_dans_l_ordre_des_pays() {
        bresil.get("/api/lots/allLotsFromAllEntrepots", "[" + lot(ID_LOT, "2026-01-10T08:30:00") + "]");
        colombie.get("/api/lots/allLotsFromAllEntrepots", "[" + lot(ID_LOT_COLOMBIE, "2026-01-05T08:30:00") + "]");
        equateur.couper();

        assertThat(adapter.findLots(TOUS)).extracting(Lot::idLot).containsExactly(ID_LOT, ID_LOT_COLOMBIE);
    }

    @Test
    void mesures_d_un_lot_sont_celles_des_pays_qui_le_possedent() {
        colombie.get("/api/lots/getAllMesuresByLotId/" + ID_LOT,
                "[{\"temperature\":27.5,\"humidite\":56.2,\"dateMesure\":\"2026-01-12T10:00:00\"},{\"temperature\":28.0,\"humidite\":null,\"dateMesure\":null}]");

        assertThat(adapter.findMesuresDuLot(TOUS, ID_LOT)).containsExactly(
                new MesureLot(27.5, 56.2, LocalDateTime.of(2026, 1, 12, 10, 0)), new MesureLot(28.0, null, null));
    }

    @Test
    void lots_les_plus_anciens_un_par_pays_qui_en_a_un() {
        bresil.get("/api/lots/oldestLot", lot(ID_LOT, "2026-01-10T08:30:00"));
        equateur.get("/api/lots/oldestLot", lot(ID_LOT_COLOMBIE, "2026-01-05T08:30:00"));

        assertThat(adapter.findLotsLesPlusAnciens(TOUS)).extracting(Lot::idLot).containsExactly(ID_LOT, ID_LOT_COLOMBIE);
    }

    // ------------------------------------------------------------------ écritures

    @Test
    void creation_envoie_le_lot_au_pays_et_renvoie_son_resultat() {
        colombie.repond("POST", "/api/lots", 201, json("{'idLot':'%s','status':'CREATED'}").formatted(ID_LOT));

        var resultat = adapter.createLot("COLOMBIE", new NouveauLot("COMPLIANT", 120, 2, LocalDate.of(2026, 3, 15), ID_EXPLOITATION, ID_ZONE));

        assertThat(resultat).contains(new ResultatLot(ID_LOT, "CREATED"));
        assertThat(colombie.requetes("/api/lots")).hasSize(1);
        assertThat(equivalents(colombie.requetes("/api/lots").get(0).corps(), json(
                "{'status':'COMPLIANT','quantite':120,'caracteristique':2,'dateProduction':'2026-03-15','idExploitation':'%s','idZone':'%s'}")
                .formatted(ID_EXPLOITATION, ID_ZONE))).isTrue();
        assertThat(bresil.requetes()).isEmpty();
    }

    @Test
    void creation_est_vide_si_le_pays_refuse_ou_est_injoignable() {
        bresil.repond("POST", "/api/lots", 500, "{}");
        colombie.couper();
        NouveauLot lot = new NouveauLot(null, null, null, null, null, null);

        assertThat(adapter.createLot("BRESIL", lot)).isEmpty();
        assertThat(adapter.createLot("COLOMBIE", lot)).isEmpty();
    }

    @Test
    void changement_de_statut_envoie_le_statut_au_pays() {
        bresil.repond("PUT", "/api/lots/" + ID_LOT + "/status", 200, json("{'idLot':'%s','status':'OK'}").formatted(ID_LOT));

        assertThat(adapter.updateStatus("BRESIL", ID_LOT, "BLOCKED")).contains(new ResultatLot(ID_LOT, "OK"));
        assertThat(bresil.requetes()).extracting(RequestRecue::methode, RequestRecue::corps).containsExactly(tuple("PUT", "{\"status\":\"BLOCKED\"}"));
    }

    @Test
    void changement_de_statut_est_vide_si_le_pays_ne_trouve_pas_le_lot() {
        bresil.repond("PUT", "/api/lots/" + ID_LOT + "/status", 404, json("{'idLot':null,'status':'NOT_FOUND'}"));

        assertThat(adapter.updateStatus("BRESIL", ID_LOT, "BLOCKED")).isEmpty();
    }

    @Test
    void transfert_de_zone_envoie_la_zone_au_pays() {
        bresil.repond("PUT", "/api/lots/" + ID_LOT + "/zone", 200, json("{'idLot':'%s','status':'OK'}").formatted(ID_LOT));

        assertThat(adapter.updateEmplacement("BRESIL", ID_LOT, ID_ZONE)).contains(new ResultatLot(ID_LOT, "OK"));
        assertThat(bresil.requetes()).extracting(RequestRecue::methode, RequestRecue::corps)
                .containsExactly(tuple("PUT", json("{'idZone':'%s'}").formatted(ID_ZONE)));
    }

    @Test
    void transfert_de_zone_signale_un_pays_indisponible_si_le_pays_refuse_ou_est_injoignable() {
        bresil.repond("PUT", "/api/lots/" + ID_LOT + "/zone", 400, "{}");
        colombie.couper();

        assertThatThrownBy(() -> adapter.updateEmplacement("BRESIL", ID_LOT, ID_ZONE)).isInstanceOf(PaysIndisponibleException.class);
        assertThatThrownBy(() -> adapter.updateEmplacement("COLOMBIE", ID_LOT, ID_ZONE)).isInstanceOf(PaysIndisponibleException.class);
    }

    @Test
    void sortie_de_stock_envoie_le_lot_en_parametre_sans_corps() {
        bresil.repond("POST", "/api/lots/stock-out", 201, json("{'idHistorique':'%s','status':'CREATED'}").formatted(ID_HISTORIQUE));

        assertThat(adapter.stockOut("BRESIL", ID_LOT)).contains(new ResultatSortieStock(ID_HISTORIQUE, "CREATED"));
        assertThat(bresil.requetes()).extracting(RequestRecue::methode, RequestRecue::query, RequestRecue::corps)
                .containsExactly(tuple("POST", "lotId=" + ID_LOT, ""));
    }

    @Test
    void sortie_de_stock_signale_un_pays_indisponible_si_le_pays_refuse() {
        bresil.repond("POST", "/api/lots/stock-out", 404, "{}");

        assertThatThrownBy(() -> adapter.stockOut("BRESIL", UUID.randomUUID())).isInstanceOf(PaysIndisponibleException.class);
    }

    @Test
    void une_ecriture_sur_un_code_de_pays_inconnu_est_une_erreur_de_programmation() {
        assertThatThrownBy(() -> adapter.createLot("ATLANTIDE", new NouveauLot(null, null, null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
