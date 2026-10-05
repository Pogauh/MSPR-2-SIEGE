package com.backendSiege.domain.services;

import com.backendSiege.domain.models.Lot;
import com.backendSiege.domain.models.MesureLot;
import com.backendSiege.domain.models.NouveauLot;
import com.backendSiege.domain.models.ResultatLot;
import com.backendSiege.domain.models.ResultatSortieStock;
import com.backendSiege.domain.ports.InventairePort;
import com.backendSiege.domain.support.Fixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.backendSiege.domain.support.Fixtures.ID_LOT;
import static com.backendSiege.domain.support.Fixtures.ID_ZONE;
import static com.backendSiege.domain.support.Fixtures.lot;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventaireServiceTest {

    private static final List<String> TOUS = List.of("BRESIL", "COLOMBIE", "EQUATEUR");
    private static final LocalDateTime JANVIER = LocalDateTime.of(2026, 1, 10, 8, 30);
    private static final LocalDateTime FEVRIER = LocalDateTime.of(2026, 2, 10, 8, 30);
    private static final LocalDateTime MARS = LocalDateTime.of(2026, 3, 10, 8, 30);

    @Mock
    InventairePort port;

    private InventaireService service() {
        return new InventaireService(port, Fixtures.paysService());
    }

    // ------------------------------------------------------------------ quantité

    @Test
    void quantiteTotale_additionne_les_quantites_de_tous_les_pays() {
        when(port.findQuantitesEnStock(TOUS)).thenReturn(List.of(100, 50, 0));

        assertThat(service().getQuantiteTotale()).isEqualTo(150);
    }

    @Test
    void quantiteTotale_est_nulle_sans_stock() {
        when(port.findQuantitesEnStock(TOUS)).thenReturn(List.of());

        assertThat(service().getQuantiteTotale()).isZero();
    }

    // ------------------------------------------------------------------ lecture

    @Test
    void getLotById_interroge_tous_les_pays() {
        Lot lot = lot(JANVIER);
        when(port.findLot(TOUS, ID_LOT)).thenReturn(Optional.of(lot));

        assertThat(service().getLotById(ID_LOT)).contains(lot);
    }

    @Test
    void getAllMesuresByLotId_interroge_tous_les_pays() {
        List<MesureLot> mesures = List.of(new MesureLot(27.5, 56.2, JANVIER));
        when(port.findMesuresDuLot(TOUS, ID_LOT)).thenReturn(mesures);

        assertThat(service().getAllMesuresByLotId(ID_LOT)).isEqualTo(mesures);
    }

    @Test
    void getAllLots_trie_du_plus_ancien_au_plus_recent_et_met_les_lots_sans_date_en_dernier() {
        Lot sansDate = lot(UUID.randomUUID(), null);
        Lot fevrier = lot(UUID.randomUUID(), FEVRIER);
        Lot janvier = lot(UUID.randomUUID(), JANVIER);
        Lot mars = lot(UUID.randomUUID(), MARS);
        when(port.findLots(TOUS)).thenReturn(List.of(sansDate, mars, janvier, fevrier));

        assertThat(service().getAllLots(null)).containsExactly(janvier, fevrier, mars, sansDate);
    }

    @Test
    void getAllLots_conserve_l_ordre_des_pays_pour_des_dates_egales() {
        Lot premier = lot(UUID.randomUUID(), JANVIER);
        Lot second = lot(UUID.randomUUID(), JANVIER);
        when(port.findLots(TOUS)).thenReturn(List.of(premier, second));

        assertThat(service().getAllLots("")).containsExactly(premier, second);
    }

    @Test
    void getAllLots_d_un_pays_n_interroge_que_ce_pays() {
        when(port.findLots(List.of("COLOMBIE"))).thenReturn(List.of());

        assertThat(service().getAllLots(" colombie ")).isEmpty();

        verify(port).findLots(List.of("COLOMBIE"));
        verifyNoMoreInteractions(port);
    }

    @Test
    void getAllLots_d_un_pays_inconnu_interroge_tous_les_pays() {
        when(port.findLots(TOUS)).thenReturn(List.of());

        service().getAllLots("atlantide");

        verify(port).findLots(TOUS);
    }

    // ------------------------------------------------------------------ lot le plus ancien

    @Test
    void getOldestLot_choisit_le_plus_ancien_de_tous_les_pays() {
        Lot janvier = lot(UUID.randomUUID(), JANVIER);
        Lot fevrier = lot(UUID.randomUUID(), FEVRIER);
        Lot mars = lot(UUID.randomUUID(), MARS);
        when(port.findLotsLesPlusAnciens(TOUS)).thenReturn(List.of(fevrier, janvier, mars));

        assertThat(service().getOldestLot(null)).contains(janvier);
    }

    @Test
    void getOldestLot_est_vide_si_aucun_pays_n_a_de_lot() {
        when(port.findLotsLesPlusAnciens(TOUS)).thenReturn(List.of());

        assertThat(service().getOldestLot("   ")).isEmpty();
    }

    @Test
    void getOldestLot_d_un_pays_n_interroge_que_ce_pays() {
        Lot janvier = lot(JANVIER);
        when(port.findLotsLesPlusAnciens(List.of("BRESIL"))).thenReturn(List.of(janvier));

        assertThat(service().getOldestLot("Bresil")).contains(janvier);

        verify(port).findLotsLesPlusAnciens(List.of("BRESIL"));
        verifyNoMoreInteractions(port);
    }

    @Test
    void getOldestLot_d_un_pays_inconnu_n_a_pas_de_lot_et_n_interroge_personne() {
        assertThat(service().getOldestLot("atlantide")).isEmpty();

        verifyNoMoreInteractions(port);
    }

    // ------------------------------------------------------------------ écritures

    @Test
    void createLot_est_transmis_au_pays_designe() {
        NouveauLot nouveau = new NouveauLot("COMPLIANT", 120, 2, LocalDate.of(2026, 3, 15), UUID.randomUUID(), ID_ZONE);
        ResultatLot resultat = new ResultatLot(ID_LOT, "CREATED");
        when(port.createLot("COLOMBIE", nouveau)).thenReturn(Optional.of(resultat));

        assertThat(service().createLot("colombie", nouveau)).contains(resultat);
    }

    @Test
    void updateStatusLot_est_transmis_au_pays_designe() {
        ResultatLot resultat = new ResultatLot(ID_LOT, "OK");
        when(port.updateStatus("BRESIL", ID_LOT, "BLOCKED")).thenReturn(Optional.of(resultat));

        assertThat(service().updateStatusLot("bresil", ID_LOT, "BLOCKED")).contains(resultat);
    }

    @Test
    void updateEmplacementLot_est_transmis_au_pays_designe() {
        ResultatLot resultat = new ResultatLot(ID_LOT, "OK");
        when(port.updateEmplacement("EQUATEUR", ID_LOT, ID_ZONE)).thenReturn(Optional.of(resultat));

        assertThat(service().updateEmplacementLot("equateur", ID_LOT, ID_ZONE)).contains(resultat);
    }

    @Test
    void stockOut_est_transmis_au_pays_designe() {
        ResultatSortieStock resultat = new ResultatSortieStock(UUID.randomUUID(), "CREATED");
        when(port.stockOut("BRESIL", ID_LOT)).thenReturn(Optional.of(resultat));

        assertThat(service().stockOut("bresil", ID_LOT)).contains(resultat);
    }

    @Test
    void les_ecritures_sur_un_pays_inconnu_ou_absent_sont_ignorees_sans_appel_au_port() {
        InventaireService service = service();

        assertThat(service.createLot("atlantide", new NouveauLot(null, null, null, null, null, null))).isEmpty();
        assertThat(service.createLot(null, new NouveauLot(null, null, null, null, null, null))).isEmpty();
        assertThat(service.updateStatusLot("", ID_LOT, "BLOCKED")).isEmpty();
        assertThat(service.updateEmplacementLot("atlantide", ID_LOT, ID_ZONE)).isEmpty();
        assertThat(service.stockOut("atlantide", ID_LOT)).isEmpty();

        verifyNoMoreInteractions(port);
    }

    @Test
    void une_ecriture_refusee_par_le_pays_donne_un_resultat_vide() {
        when(port.createLot(any(), any())).thenReturn(Optional.empty());

        assertThat(service().createLot("bresil", new NouveauLot(null, null, null, null, null, null))).isEmpty();
    }
}
