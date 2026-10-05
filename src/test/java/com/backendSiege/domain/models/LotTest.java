package com.backendSiege.domain.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.backendSiege.domain.support.Fixtures.lot;
import static org.assertj.core.api.Assertions.assertThat;

class LotTest {

    private static final LocalDateTime JANVIER = LocalDateTime.of(2026, 1, 10, 8, 30);
    private static final LocalDateTime FEVRIER = LocalDateTime.of(2026, 2, 10, 8, 30);

    @Test
    void lePlusAncien_est_le_lot_stocke_depuis_le_plus_longtemps() {
        Lot janvier = lot(JANVIER);
        Lot fevrier = lot(FEVRIER);

        assertThat(Lot.lePlusAncien(janvier, fevrier)).isSameAs(janvier);
        assertThat(Lot.lePlusAncien(fevrier, janvier)).isSameAs(janvier);
    }

    @Test
    void lePlusAncien_cede_la_place_a_l_autre_lot_s_il_n_a_pas_de_date_de_stockage() {
        Lot sansDate = lot(null);
        Lot janvier = lot(JANVIER);

        assertThat(Lot.lePlusAncien(sansDate, janvier)).isSameAs(janvier);
        assertThat(Lot.lePlusAncien(janvier, sansDate)).isSameAs(janvier);
    }

    @Test
    void lePlusAncien_de_deux_lots_sans_date_est_le_second() {
        Lot premier = lot(null);
        Lot second = lot(null);

        assertThat(Lot.lePlusAncien(premier, second)).isSameAs(second);
    }

    @Test
    void lePlusAncien_a_date_egale_est_le_second() {
        Lot premier = lot(JANVIER);
        Lot second = lot(JANVIER);

        assertThat(Lot.lePlusAncien(premier, second)).isSameAs(second);
    }
}
