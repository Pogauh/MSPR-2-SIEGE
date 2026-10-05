package com.backendSiege.domain.services;

import com.backendSiege.domain.models.CapteurSummary;
import com.backendSiege.domain.models.Mesure;
import com.backendSiege.domain.ports.MesurePort;
import com.backendSiege.domain.support.Fixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.backendSiege.domain.support.Fixtures.ID_ENTREPOT;
import static com.backendSiege.domain.support.Fixtures.ID_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesureServiceTest {

    private static final LocalDateTime DEBUT = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 1, 31, 0, 0);
    private static final Mesure MESURE = new Mesure(UUID.randomUUID(), 27.5, 56.2, DEBUT, UUID.randomUUID());

    @Mock
    MesurePort port;

    private MesureService service() {
        return new MesureService(port, Fixtures.paysService());
    }

    @Test
    void getMesureById_interroge_le_pays_designe() {
        when(port.findMesure("BRESIL", MESURE.idMesure())).thenReturn(Optional.of(MESURE));

        assertThat(service().getMesureById("bresil", MESURE.idMesure())).contains(MESURE);
    }

    @Test
    void getMesuresByZoneId_interroge_le_pays_designe_sur_la_periode() {
        when(port.findMesuresDeLaZone("COLOMBIE", ID_ZONE, DEBUT, FIN)).thenReturn(List.of(MESURE));

        assertThat(service().getMesuresByZoneId("colombie", ID_ZONE, DEBUT, FIN)).containsExactly(MESURE);
    }

    @Test
    void getLastMesureByZoneId_interroge_le_pays_designe() {
        when(port.findDerniereMesureDeLaZone("EQUATEUR", ID_ZONE)).thenReturn(Optional.of(MESURE));

        assertThat(service().getLastMesureByZoneId("equateur", ID_ZONE)).contains(MESURE);
    }

    @Test
    void getCapteurSummaryByEntrepotId_interroge_le_pays_designe() {
        CapteurSummary resume = new CapteurSummary(ID_ZONE, "A", new BigDecimal("27.50"), new BigDecimal("56.20"),
                new BigDecimal("27.1"), new BigDecimal("56"));
        when(port.findResumeCapteurs("BRESIL", ID_ENTREPOT)).thenReturn(List.of(resume));

        assertThat(service().getCapteurSummaryByEntrepotId("Bresil", ID_ENTREPOT)).containsExactly(resume);
    }

    @Test
    void un_pays_inconnu_ou_absent_n_a_ni_mesure_ni_capteur_et_n_interroge_personne() {
        MesureService service = service();

        assertThat(service.getMesureById("atlantide", UUID.randomUUID())).isEmpty();
        assertThat(service.getMesuresByZoneId("atlantide", ID_ZONE, DEBUT, FIN)).isEmpty();
        assertThat(service.getLastMesureByZoneId(null, ID_ZONE)).isEmpty();
        assertThat(service.getCapteurSummaryByEntrepotId("", ID_ENTREPOT)).isEmpty();

        verifyNoInteractions(port);
    }
}
