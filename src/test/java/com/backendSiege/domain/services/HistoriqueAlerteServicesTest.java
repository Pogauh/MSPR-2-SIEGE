package com.backendSiege.domain.services;

import com.backendSiege.domain.models.Alerte;
import com.backendSiege.domain.models.Historique;
import com.backendSiege.domain.models.TypeHistoriqueEnum;
import com.backendSiege.domain.ports.AlertePort;
import com.backendSiege.domain.ports.HistoriquePort;
import com.backendSiege.domain.support.Fixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.backendSiege.domain.support.Fixtures.ID_ENTREPOT;
import static com.backendSiege.domain.support.Fixtures.ID_LOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoriqueAlerteServicesTest {

    private static final List<String> TOUS = List.of("BRESIL", "COLOMBIE", "EQUATEUR");

    @Mock
    HistoriquePort historiquePort;
    @Mock
    AlertePort alertePort;

    @Test
    void historique_est_demande_a_tous_les_pays_sans_filtre() {
        List<Historique> historique = List.of(new Historique(UUID.randomUUID(), "TRANSFERT", LocalDateTime.now(), null, null, "d"));
        when(historiquePort.findHistorique(TOUS, ID_LOT, null)).thenReturn(historique);

        assertThat(new HistoriqueService(historiquePort, Fixtures.paysService()).findAllHistoriqueForLot(ID_LOT, null))
                .isEqualTo(historique);
    }

    @Test
    void historique_est_demande_a_tous_les_pays_avec_le_filtre_de_type() {
        when(historiquePort.findHistorique(TOUS, ID_LOT, TypeHistoriqueEnum.ALERTE)).thenReturn(List.of());

        assertThat(new HistoriqueService(historiquePort, Fixtures.paysService())
                .findAllHistoriqueForLot(ID_LOT, TypeHistoriqueEnum.ALERTE)).isEmpty();
    }

    @Test
    void alertes_sont_demandees_a_tous_les_pays_sur_la_periode() {
        LocalDateTime debut = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 1, 2, 0, 0);
        List<Alerte> alertes = List.of(new Alerte("a1", "HAUTETEMPERATURE", "trop chaud", debut, "zone"));
        when(alertePort.findAlertes(TOUS, ID_ENTREPOT, debut, fin)).thenReturn(alertes);

        assertThat(new AlerteService(alertePort, Fixtures.paysService()).getAllAlertesByEntrepotId(ID_ENTREPOT, debut, fin))
                .isEqualTo(alertes);
    }

    @Test
    void alertes_sans_periode_ne_bornent_pas_la_recherche() {
        when(alertePort.findAlertes(TOUS, ID_ENTREPOT, null, null)).thenReturn(List.of());

        assertThat(new AlerteService(alertePort, Fixtures.paysService()).getAllAlertesByEntrepotId(ID_ENTREPOT, null, null)).isEmpty();
    }
}
