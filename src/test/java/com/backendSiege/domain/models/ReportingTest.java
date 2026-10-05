package com.backendSiege.domain.models;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** Règles de consolidation des rapports des pays. */
class ReportingTest {

    // ------------------------------------------------------------------ global

    @Test
    void global_somme_les_lots_et_les_mouvements() {
        ReportingGlobal consolide = ReportingGlobal.consolider(List.of(
                new ReportingGlobal(10, 30.0, 80.0, 4), new ReportingGlobal(30, 50.0, 100.0, 6)));

        assertThat(consolide.totalLots()).isEqualTo(40);
        assertThat(consolide.countMouvementLast30Days()).isEqualTo(10);
    }

    @Test
    void global_pondere_l_age_moyen_et_la_conformite_par_le_nombre_de_lots() {
        ReportingGlobal consolide = ReportingGlobal.consolider(List.of(
                new ReportingGlobal(10, 30.0, 80.0, 0), new ReportingGlobal(30, 50.0, 100.0, 0)));

        assertThat(consolide.avgAgeDays()).isEqualTo(45.0);
        assertThat(consolide.complianceRate()).isEqualTo(95.0);
    }

    @Test
    void global_ne_depend_pas_de_l_ordre_des_pays() {
        ReportingGlobal a = new ReportingGlobal(7, 12.3, 45.6, 1);
        ReportingGlobal b = new ReportingGlobal(13, 40.1, 99.9, 2);
        ReportingGlobal c = new ReportingGlobal(5, 3.3, 10.0, 3);

        ReportingGlobal abc = ReportingGlobal.consolider(List.of(a, b, c));
        ReportingGlobal cba = ReportingGlobal.consolider(List.of(c, b, a));

        assertThat(abc.totalLots()).isEqualTo(cba.totalLots());
        assertThat(abc.avgAgeDays()).isCloseTo(cba.avgAgeDays(), within(1e-9));
        assertThat(abc.complianceRate()).isCloseTo(cba.complianceRate(), within(1e-9));
    }

    @Test
    void global_ignore_un_pays_vide() {
        ReportingGlobal consolide = ReportingGlobal.consolider(List.of(
                ReportingGlobal.vide(), new ReportingGlobal(10, 30.0, 80.0, 4), ReportingGlobal.vide()));

        assertThat(consolide).isEqualTo(new ReportingGlobal(10, 30.0, 80.0, 4));
    }

    @Test
    void global_sans_pays_ou_sans_lot_donne_des_zeros() {
        assertThat(ReportingGlobal.consolider(List.of())).isEqualTo(ReportingGlobal.vide());
        assertThat(ReportingGlobal.consolider(List.of(ReportingGlobal.vide(), ReportingGlobal.vide()))).isEqualTo(ReportingGlobal.vide());
        assertThat(ReportingGlobal.vide()).isEqualTo(new ReportingGlobal(0, 0, 0, 0));
    }

    // ------------------------------------------------------------------ qualité

    @Test
    void qualite_somme_les_alertes_et_les_lots_non_conformes() {
        ReportingQuality consolide = ReportingQuality.consolider(List.of(
                new ReportingQuality(2, 80.0, 1), new ReportingQuality(3, 60.0, 2)));

        assertThat(consolide.alertCount()).isEqualTo(5);
        assertThat(consolide.nonComplianceCount()).isEqualTo(3);
    }

    @Test
    void qualite_moyenne_les_taux_de_deux_pays() {
        assertThat(ReportingQuality.consolider(List.of(new ReportingQuality(0, 80.0, 0), new ReportingQuality(0, 60.0, 0))).complianceRate())
                .isEqualTo(70.0);
    }

    @Test
    void qualite_ignore_les_taux_nuls() {
        ReportingQuality consolide = ReportingQuality.consolider(List.of(
                ReportingQuality.vide(), new ReportingQuality(1, 80.0, 0), ReportingQuality.vide(), new ReportingQuality(1, 60.0, 0)));

        assertThat(consolide.complianceRate()).isEqualTo(70.0);
    }

    /** Comportement historique : au-delà de deux pays, chaque nouveau taux est moyenné avec le résultat courant. */
    @Test
    void qualite_est_une_moyenne_glissante_et_non_arithmetique_a_partir_de_trois_pays() {
        ReportingQuality consolide = ReportingQuality.consolider(List.of(
                new ReportingQuality(0, 100.0, 0), new ReportingQuality(0, 50.0, 0), new ReportingQuality(0, 20.0, 0)));

        assertThat(consolide.complianceRate()).isEqualTo(47.5).isNotEqualTo((100.0 + 50.0 + 20.0) / 3);
    }

    @Test
    void qualite_sans_pays_donne_des_zeros() {
        assertThat(ReportingQuality.consolider(List.of())).isEqualTo(ReportingQuality.vide()).isEqualTo(new ReportingQuality(0, 0, 0));
    }

    // ------------------------------------------------------------------ stock

    @Test
    void stock_d_un_pays_sans_reponse_est_vide_sans_liste_de_lots_urgents() {
        ReportingStock vide = ReportingStock.vide();

        assertThat(vide.totalLots()).isZero();
        assertThat(vide.avgAgeDays()).isZero();
        assertThat(vide.complianceRate()).isZero();
        assertThat(vide.lotIdsUrgentFifo()).isNull();
        assertThat(new ReportingStock(0, 0, 0, List.of(UUID.randomUUID())).lotIdsUrgentFifo()).hasSize(1);
    }
}
