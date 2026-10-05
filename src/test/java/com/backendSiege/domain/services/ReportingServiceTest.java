package com.backendSiege.domain.services;

import com.backendSiege.domain.exceptions.PaysIntrouvableException;
import com.backendSiege.domain.exceptions.PaysNonSupporteException;
import com.backendSiege.domain.exceptions.PaysObligatoireException;
import com.backendSiege.domain.models.ReportingGlobal;
import com.backendSiege.domain.models.ReportingQuality;
import com.backendSiege.domain.models.ReportingStock;
import com.backendSiege.domain.ports.ReportingPort;
import com.backendSiege.domain.support.Fixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    private static final List<String> TOUS = List.of("BRESIL", "COLOMBIE", "EQUATEUR");
    private static final LocalDateTime DEBUT = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 1, 31, 0, 0);

    @Mock
    ReportingPort port;

    private ReportingService service() {
        return new ReportingService(port, Fixtures.paysService());
    }

    @Test
    void reportingGlobal_consolide_les_rapports_de_tous_les_pays() {
        when(port.findReportingGlobal(TOUS)).thenReturn(List.of(
                new ReportingGlobal(10, 30.0, 80.0, 4), new ReportingGlobal(30, 50.0, 100.0, 6), ReportingGlobal.vide()));

        assertThat(service().getReportingGlobal()).isEqualTo(new ReportingGlobal(40, 45.0, 95.0, 10));
    }

    @Test
    void reportingQuality_sans_pays_consolide_les_rapports_de_tous_les_pays() {
        when(port.findReportingQuality(TOUS, DEBUT, FIN)).thenReturn(List.of(
                new ReportingQuality(2, 80.0, 1), new ReportingQuality(3, 60.0, 2), ReportingQuality.vide()));

        assertThat(service().getReportingQuality(null, DEBUT, FIN)).isEqualTo(new ReportingQuality(5, 70.0, 3));
        assertThat(service().getReportingQuality("  ", DEBUT, FIN)).isEqualTo(new ReportingQuality(5, 70.0, 3));
    }

    @Test
    void reportingQuality_d_un_pays_renvoie_son_rapport_tel_quel() {
        ReportingQuality rapport = new ReportingQuality(2, 80.0, 1);
        when(port.findReportingQuality(List.of("BRESIL"), DEBUT, FIN)).thenReturn(List.of(rapport));

        assertThat(service().getReportingQuality(" bresil ", DEBUT, FIN)).isSameAs(rapport);
    }

    @Test
    void reportingQuality_d_un_pays_sans_reponse_du_port_est_vide() {
        when(port.findReportingQuality(List.of("BRESIL"), DEBUT, FIN)).thenReturn(List.of());

        assertThat(service().getReportingQuality("bresil", DEBUT, FIN)).isEqualTo(ReportingQuality.vide());
    }

    @Test
    void reportingQuality_d_un_pays_inconnu_est_refuse() {
        assertThatThrownBy(() -> service().getReportingQuality("atlantide", DEBUT, FIN))
                .isInstanceOf(PaysNonSupporteException.class)
                .hasMessageContaining("atlantide");

        verifyNoInteractions(port);
    }

    @Test
    void reportingStock_est_celui_du_pays_designe() {
        ReportingStock stock = new ReportingStock(3, 12.5, 66.7, List.of(UUID.randomUUID()));
        when(port.findReportingStock("COLOMBIE", DEBUT, FIN)).thenReturn(stock);

        assertThat(service().getReportingStock("Colombie", DEBUT, FIN)).isSameAs(stock);
    }

    @Test
    void reportingStock_exige_un_pays() {
        for (String pays : new String[]{null, "", "   "}) {
            assertThatThrownBy(() -> service().getReportingStock(pays, DEBUT, FIN)).isInstanceOf(PaysObligatoireException.class);
        }

        verifyNoInteractions(port);
    }

    @Test
    void reportingStock_d_un_pays_inconnu_est_introuvable() {
        assertThatThrownBy(() -> service().getReportingStock("atlantide", DEBUT, FIN))
                .isInstanceOf(PaysIntrouvableException.class)
                .hasMessageContaining("atlantide");

        verifyNoInteractions(port);
    }
}
