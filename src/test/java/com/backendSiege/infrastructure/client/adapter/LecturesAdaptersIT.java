package com.backendSiege.infrastructure.client.adapter;

import com.backendSiege.domain.exceptions.PaysIndisponibleException;
import com.backendSiege.domain.models.Alerte;
import com.backendSiege.domain.models.CapteurSummary;
import com.backendSiege.domain.models.Entrepot;
import com.backendSiege.domain.models.Exploitation;
import com.backendSiege.domain.models.Historique;
import com.backendSiege.domain.models.Mesure;
import com.backendSiege.domain.models.ReportingGlobal;
import com.backendSiege.domain.models.ReportingQuality;
import com.backendSiege.domain.models.ReportingStock;
import com.backendSiege.domain.models.TypeHistoriqueEnum;
import com.backendSiege.domain.models.Zone;
import com.backendSiege.infrastructure.client.PaysClients;
import com.backendSiege.infrastructure.client.mapper.PaysMapperImpl;
import com.backendSiege.support.FakePaysServer;
import com.backendSiege.support.FakePaysServer.RequestRecue;
import com.backendSiege.support.PaysTestClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.backendSiege.support.Donnees.ID_CAPTEUR;
import static com.backendSiege.support.Donnees.ID_ENTREPOT;
import static com.backendSiege.support.Donnees.ID_EXPLOITATION;
import static com.backendSiege.support.Donnees.ID_HISTORIQUE;
import static com.backendSiege.support.Donnees.ID_LOT;
import static com.backendSiege.support.Donnees.ID_MESURE;
import static com.backendSiege.support.Donnees.ID_ZONE;
import static com.backendSiege.support.Json.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

/** Adaptateurs de lecture des backends pays (mesures, référentiel, historique, alertes, reporting) face à de faux backends HTTP. */
class LecturesAdaptersIT {

    private static final List<String> TOUS = List.of("BRESIL", "COLOMBIE", "EQUATEUR");
    private static final LocalDateTime DEBUT = LocalDateTime.of(2026, 1, 1, 10, 0, 30);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 1, 2, 0, 0);

    Map<String, FakePaysServer> serveurs;
    FakePaysServer bresil;
    FakePaysServer colombie;
    FakePaysServer equateur;
    PaysClients clients;
    PaysMapperImpl mapper = new PaysMapperImpl();

    @BeforeEach
    void demarrer() {
        serveurs = PaysTestClients.troisServeurs();
        bresil = serveurs.get("BRESIL");
        colombie = serveurs.get("COLOMBIE");
        equateur = serveurs.get("EQUATEUR");
        clients = PaysTestClients.clients(serveurs);
    }

    @AfterEach
    void arreter() {
        serveurs.values().forEach(FakePaysServer::couper);
    }

    // ------------------------------------------------------------------ mesures

    @Test
    void mesure_est_lue_par_son_identifiant() {
        MesureAdapter adapter = new MesureAdapter(clients, mapper);
        bresil.get("/api/mesures/getMesure/" + ID_MESURE, json(
                "{'idMesure':'%s','temperature':27.5,'humidite':56.2,'dateMesure':'2026-01-12T10:00:00','idCapteur':'%s'}").formatted(ID_MESURE, ID_CAPTEUR));

        assertThat(adapter.findMesure("BRESIL", ID_MESURE))
                .contains(new Mesure(ID_MESURE, 27.5, 56.2, LocalDateTime.of(2026, 1, 12, 10, 0), ID_CAPTEUR));
    }

    @Test
    void mesure_est_vide_si_le_pays_ne_la_connait_pas_ou_ne_repond_pas() {
        MesureAdapter adapter = new MesureAdapter(clients, mapper);
        colombie.couper();

        assertThat(adapter.findMesure("BRESIL", ID_MESURE)).isEmpty();
        assertThat(adapter.findMesure("COLOMBIE", ID_MESURE)).isEmpty();
    }

    @Test
    void mesures_d_une_zone_sont_demandees_avec_la_periode_au_format_du_pays() {
        MesureAdapter adapter = new MesureAdapter(clients, mapper);
        bresil.get("/api/zone/readings/" + ID_ZONE, "[{\"idMesure\":\"" + ID_MESURE + "\",\"temperature\":27.5,\"humidite\":56.2,"
                + "\"dateMesure\":\"2026-01-12T10:00:00\",\"idCapteur\":\"" + ID_CAPTEUR + "\"}]");

        assertThat(adapter.findMesuresDeLaZone("BRESIL", ID_ZONE, DEBUT, FIN)).hasSize(1);
        assertThat(bresil.requetes()).extracting(RequestRecue::chemin, RequestRecue::query)
                .containsExactly(tuple("/api/zone/readings/" + ID_ZONE, "startDate=2026-01-01T10:00:30&endDate=2026-01-02T00:00"));
    }

    @Test
    void mesures_d_une_zone_sans_periode_ne_bornent_pas_la_recherche() {
        MesureAdapter adapter = new MesureAdapter(clients, mapper);
        bresil.get("/api/zone/readings/" + ID_ZONE, "[]");

        assertThat(adapter.findMesuresDeLaZone("BRESIL", ID_ZONE, null, null)).isEmpty();
        assertThat(bresil.requetes()).extracting(RequestRecue::query).containsExactly((String) null);
    }

    @Test
    void mesures_d_une_zone_sont_vides_si_le_pays_ne_repond_pas() {
        MesureAdapter adapter = new MesureAdapter(clients, mapper);
        bresil.couper();

        assertThat(adapter.findMesuresDeLaZone("BRESIL", ID_ZONE, DEBUT, FIN)).isEmpty();
    }

    @Test
    void derniere_mesure_d_une_zone() {
        MesureAdapter adapter = new MesureAdapter(clients, mapper);
        bresil.get("/api/zone/" + ID_ZONE + "/readings/latest", json(
                "{'idMesure':'%s','temperature':27.5,'humidite':56.2,'dateMesure':'2026-01-12T10:00:00','idCapteur':'%s'}").formatted(ID_MESURE, ID_CAPTEUR));

        assertThat(adapter.findDerniereMesureDeLaZone("BRESIL", ID_ZONE)).hasValueSatisfying(m -> assertThat(m.idMesure()).isEqualTo(ID_MESURE));
        assertThat(adapter.findDerniereMesureDeLaZone("COLOMBIE", ID_ZONE)).isEmpty();
    }

    @Test
    void resume_des_capteurs_conserve_les_decimales_du_pays() {
        MesureAdapter adapter = new MesureAdapter(clients, mapper);
        bresil.get("/api/entrepots/" + ID_ENTREPOT + "/capteurSummary", json(
                "[{'zoneId':'%s','zoneName':'A','lastTemp':27.50,'lastHumidity':56.20,'avgTemp24h':27.125,'avgHumidity24h':56}]").formatted(ID_ZONE));

        List<CapteurSummary> resume = adapter.findResumeCapteurs("BRESIL", ID_ENTREPOT);

        assertThat(resume).containsExactly(new CapteurSummary(ID_ZONE, "A", new BigDecimal("27.50"), new BigDecimal("56.20"),
                new BigDecimal("27.125"), new BigDecimal("56")));
        assertThat(resume.get(0).lastTemp().scale()).isEqualTo(2);
        assertThat(adapter.findResumeCapteurs("COLOMBIE", ID_ENTREPOT)).isEmpty();
    }

    // ------------------------------------------------------------------ référentiel

    @Test
    void referentiel_est_lu_dans_le_pays_demande() {
        ReferentielAdapter adapter = new ReferentielAdapter(clients, mapper);
        bresil.get("/api/exploitation/allExploitations", json("[{'idExploitation':'%s','nom':'Ferme','adresse':'Rue 1'}]").formatted(ID_EXPLOITATION));
        bresil.get("/api/entrepots", json("[{'idEntrepot':'%s','localisation':'Santos','nom':'Principal'}]").formatted(ID_ENTREPOT));
        bresil.get("/api/entrepots/" + ID_ENTREPOT + "/zones", json(
                "[{'idZone':'%s','nomZone':'A','idEntrepot':'%s','idCapteur':null,'lotCount':3}]").formatted(ID_ZONE, ID_ENTREPOT));

        assertThat(adapter.findExploitations("BRESIL")).containsExactly(new Exploitation(ID_EXPLOITATION, "Ferme", "Rue 1"));
        assertThat(adapter.findEntrepots("BRESIL")).containsExactly(new Entrepot(ID_ENTREPOT, "Santos", "Principal"));
        assertThat(adapter.findZones("BRESIL", ID_ENTREPOT)).containsExactly(new Zone(ID_ZONE, "A", ID_ENTREPOT, null, 3L));
        assertThat(colombie.requetes()).isEmpty();
    }

    @Test
    void referentiel_signale_un_pays_indisponible_s_il_ne_repond_pas() {
        ReferentielAdapter adapter = new ReferentielAdapter(clients, mapper);
        colombie.couper();

        assertThatThrownBy(() -> adapter.findExploitations("BRESIL")).isInstanceOf(PaysIndisponibleException.class);
        assertThatThrownBy(() -> adapter.findEntrepots("COLOMBIE")).isInstanceOf(PaysIndisponibleException.class);
        assertThatThrownBy(() -> adapter.findZones("COLOMBIE", ID_ENTREPOT)).isInstanceOf(PaysIndisponibleException.class);
    }

    @Test
    void referentiel_d_un_pays_sans_donnee_est_vide() {
        ReferentielAdapter adapter = new ReferentielAdapter(clients, mapper);
        bresil.get("/api/entrepots", "[]");

        assertThat(adapter.findEntrepots("BRESIL")).isEmpty();
    }

    // ------------------------------------------------------------------ historique

    @Test
    void historique_est_demande_a_tous_les_pays_avec_le_type_en_parametre() {
        HistoriqueAdapter adapter = new HistoriqueAdapter(clients, mapper);
        colombie.get("/api/historique/allHistoriquesForLot/" + ID_LOT, json(
                "[{'idHistorique':'%s','type':'ALERTE','dateEmission':'2026-01-11T09:00:00','zonePrecedente':null,'zoneActuelle':'%s','detail':'d'}]")
                .formatted(ID_HISTORIQUE, ID_ZONE));

        List<Historique> historique = adapter.findHistorique(TOUS, ID_LOT, TypeHistoriqueEnum.ALERTE);

        assertThat(historique).containsExactly(new Historique(ID_HISTORIQUE, "ALERTE", LocalDateTime.of(2026, 1, 11, 9, 0), null, ID_ZONE, "d"));
        for (FakePaysServer pays : serveurs.values()) {
            assertThat(pays.requetes()).extracting(RequestRecue::query).containsExactly("type=ALERTE");
        }
    }

    @Test
    void historique_sans_type_n_envoie_pas_de_parametre_et_ignore_les_pays_en_erreur() {
        HistoriqueAdapter adapter = new HistoriqueAdapter(clients, mapper);
        bresil.couper();
        colombie.repond("GET", "/api/historique/allHistoriquesForLot/" + ID_LOT, 500, "{}");

        assertThat(adapter.findHistorique(TOUS, ID_LOT, null)).isEmpty();
        assertThat(equateur.requetes()).extracting(RequestRecue::query).containsExactly((String) null);
    }

    // ------------------------------------------------------------------ alertes

    @Test
    void alertes_sont_demandees_a_tous_les_pays_avec_l_entrepot_et_la_periode() {
        AlerteAdapter adapter = new AlerteAdapter(clients, mapper);
        bresil.get("/api/alertes", "[{\"idAlerte\":\"a1\",\"typeAlerte\":\"HAUTETEMPERATURE\",\"description\":\"d\","
                + "\"dateEmission\":\"2026-01-12T10:05:00\",\"idZone\":\"" + ID_ZONE + "\"}]");

        assertThat(adapter.findAlertes(TOUS, ID_ENTREPOT, DEBUT, FIN))
                .containsExactly(new Alerte("a1", "HAUTETEMPERATURE", "d", LocalDateTime.of(2026, 1, 12, 10, 5), ID_ZONE.toString()));
        for (FakePaysServer pays : serveurs.values()) {
            assertThat(pays.requetes()).extracting(RequestRecue::query)
                    .containsExactly("idEntrepot=" + ID_ENTREPOT + "&startDate=2026-01-01T10:00:30&endDate=2026-01-02T00:00");
        }
    }

    @Test
    void alertes_sans_periode_n_envoient_que_l_entrepot_et_ignorent_les_pays_en_erreur() {
        AlerteAdapter adapter = new AlerteAdapter(clients, mapper);
        bresil.couper();
        colombie.repond("GET", "/api/alertes", 500, "{}");

        assertThat(adapter.findAlertes(TOUS, ID_ENTREPOT, null, null)).isEmpty();
        assertThat(equateur.requetes()).extracting(RequestRecue::query).containsExactly("idEntrepot=" + ID_ENTREPOT);
    }

    // ------------------------------------------------------------------ reporting

    @Test
    void reporting_global_un_rapport_par_pays_dans_l_ordre_les_pays_en_erreur_donnent_un_rapport_vide() {
        ReportingAdapter adapter = new ReportingAdapter(clients, mapper);
        bresil.get("/api/reporting/global", json("{'totalLots':10,'avgAgeDays':30.0,'complianceRate':80.0,'countMouvementLast30Days':4}"));
        colombie.repond("GET", "/api/reporting/global", 500, "{}");
        equateur.couper();

        assertThat(adapter.findReportingGlobal(TOUS)).containsExactly(new ReportingGlobal(10, 30.0, 80.0, 4), ReportingGlobal.vide(), ReportingGlobal.vide());
    }

    @Test
    void reporting_qualite_un_rapport_par_pays_avec_la_periode_au_format_du_pays() {
        ReportingAdapter adapter = new ReportingAdapter(clients, mapper);
        bresil.get("/api/reporting/quality", json("{'alertCount':2,'complianceRate':80.0,'nonComplianceCount':1}"));
        colombie.get("/api/reporting/quality", json("{'alertCount':3,'complianceRate':60.0,'nonComplianceCount':2}"));

        assertThat(adapter.findReportingQuality(List.of("BRESIL", "COLOMBIE", "EQUATEUR"), DEBUT, FIN))
                .containsExactly(new ReportingQuality(2, 80.0, 1), new ReportingQuality(3, 60.0, 2), ReportingQuality.vide());
        assertThat(bresil.requetes()).extracting(RequestRecue::query).containsExactly("startDate=2026-01-01T10:00:30&endDate=2026-01-02T00:00");
    }

    @Test
    void reporting_stock_est_celui_du_pays_demande_ou_vide_s_il_ne_repond_pas() {
        ReportingAdapter adapter = new ReportingAdapter(clients, mapper);
        UUID urgent = UUID.randomUUID();
        bresil.get("/api/reporting/stock", json("{'totalLots':3,'avgAgeDays':12.5,'complianceRate':66.7,'lotIdsUrgentFifo':['%s']}").formatted(urgent));
        colombie.couper();

        assertThat(adapter.findReportingStock("BRESIL", DEBUT, FIN)).isEqualTo(new ReportingStock(3, 12.5, 66.7, List.of(urgent)));
        assertThat(adapter.findReportingStock("COLOMBIE", DEBUT, FIN)).isEqualTo(ReportingStock.vide());
        assertThat(adapter.findReportingStock("EQUATEUR", DEBUT, FIN)).isEqualTo(ReportingStock.vide());
    }
}
