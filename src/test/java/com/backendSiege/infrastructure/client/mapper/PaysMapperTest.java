package com.backendSiege.infrastructure.client.mapper;

import com.backendSiege.domain.models.Alerte;
import com.backendSiege.domain.models.CapteurSummary;
import com.backendSiege.domain.models.Entrepot;
import com.backendSiege.domain.models.Exploitation;
import com.backendSiege.domain.models.Historique;
import com.backendSiege.domain.models.Lot;
import com.backendSiege.domain.models.Mesure;
import com.backendSiege.domain.models.MesureLot;
import com.backendSiege.domain.models.NouveauLot;
import com.backendSiege.domain.models.ReportingGlobal;
import com.backendSiege.domain.models.ReportingQuality;
import com.backendSiege.domain.models.ReportingStock;
import com.backendSiege.domain.models.ResultatLot;
import com.backendSiege.domain.models.ResultatSortieStock;
import com.backendSiege.domain.models.Zone;
import com.backendSiege.infrastructure.client.dto.AlertePaysDto;
import com.backendSiege.infrastructure.client.dto.CapteurSummaryPaysDto;
import com.backendSiege.infrastructure.client.dto.EntrepotPaysDto;
import com.backendSiege.infrastructure.client.dto.ExploitationPaysDto;
import com.backendSiege.infrastructure.client.dto.HistoriquePaysDto;
import com.backendSiege.infrastructure.client.dto.LotPaysDto;
import com.backendSiege.infrastructure.client.dto.MesureLotPaysDto;
import com.backendSiege.infrastructure.client.dto.MesurePaysDto;
import com.backendSiege.infrastructure.client.dto.NouveauLotPaysDto;
import com.backendSiege.infrastructure.client.dto.ReportingGlobalPaysDto;
import com.backendSiege.infrastructure.client.dto.ReportingQualityPaysDto;
import com.backendSiege.infrastructure.client.dto.ReportingStockPaysDto;
import com.backendSiege.infrastructure.client.dto.ResultatLotPaysDto;
import com.backendSiege.infrastructure.client.dto.ResultatSortieStockPaysDto;
import com.backendSiege.infrastructure.client.dto.ZonePaysDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Traduction du contrat des API des backends pays vers le domaine : chaque champ est reporté tel quel, sans valeur par défaut. */
class PaysMapperTest {

    private static final UUID ID_LOT = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ID_ZONE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ID_HISTORIQUE = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ID_ENTREPOT = UUID.fromString("770e8400-e29b-41d4-a716-446655440011");
    private static final UUID ID_EXPLOITATION = UUID.fromString("aa0e8400-e29b-41d4-a716-446655440022");
    private static final UUID ID_CAPTEUR = UUID.fromString("b793e242-053a-4def-9cd8-96c990a586c9");
    private static final LocalDateTime DATE = LocalDateTime.of(2026, 1, 12, 10, 0);

    private final PaysMapper mapper = new PaysMapperImpl();

    @Test
    void lot_et_son_historique() {
        LotPaysDto dto = new LotPaysDto(ID_LOT, "ALERT", 120, 2, DATE, DATE.minusDays(10), LocalDate.of(2025, 12, 1), ID_ZONE.toString(),
                ID_EXPLOITATION, ID_ENTREPOT, List.of(new HistoriquePaysDto(ID_HISTORIQUE, "ALERTE", DATE, ID_ZONE, null, "Alerte")));

        assertThat(mapper.toLot(dto)).isEqualTo(new Lot(ID_LOT, "ALERT", 120, 2, DATE, DATE.minusDays(10), LocalDate.of(2025, 12, 1),
                ID_ZONE.toString(), ID_EXPLOITATION, ID_ENTREPOT, List.of(new Historique(ID_HISTORIQUE, "ALERTE", DATE, ID_ZONE, null, "Alerte"))));
    }

    @Test
    void lot_vide_reste_vide_sans_valeur_par_defaut_ni_liste_vide() {
        Lot lot = mapper.toLot(new LotPaysDto(null, null, null, null, null, null, null, null, null, null, null));

        assertThat(lot).isEqualTo(new Lot(null, null, null, null, null, null, null, null, null, null, null));
        assertThat(lot.historique()).isNull();
    }

    @Test
    void mesures() {
        assertThat(mapper.toMesureLot(new MesureLotPaysDto(27.5, null, DATE))).isEqualTo(new MesureLot(27.5, null, DATE));
        assertThat(mapper.toMesure(new MesurePaysDto(ID_LOT, 27.5, 56.2, DATE, ID_CAPTEUR))).isEqualTo(new Mesure(ID_LOT, 27.5, 56.2, DATE, ID_CAPTEUR));
    }

    @Test
    void resume_de_capteur_conserve_les_decimales() {
        CapteurSummary resume = mapper.toCapteurSummary(new CapteurSummaryPaysDto(ID_ZONE, "A", new BigDecimal("27.50"), new BigDecimal("56.20"),
                new BigDecimal("27.125"), new BigDecimal("56")));

        assertThat(resume.lastTemp()).isEqualTo(new BigDecimal("27.50"));
        assertThat(resume.lastTemp().scale()).isEqualTo(2);
        assertThat(resume.avgHumidity24h()).isEqualTo(new BigDecimal("56"));
    }

    @Test
    void referentiel_et_alertes() {
        assertThat(mapper.toEntrepot(new EntrepotPaysDto(ID_ENTREPOT, "Santos", "Principal"))).isEqualTo(new Entrepot(ID_ENTREPOT, "Santos", "Principal"));
        assertThat(mapper.toExploitation(new ExploitationPaysDto(ID_EXPLOITATION, "Ferme", "Rue 1")))
                .isEqualTo(new Exploitation(ID_EXPLOITATION, "Ferme", "Rue 1"));
        assertThat(mapper.toZone(new ZonePaysDto(ID_ZONE, "A", ID_ENTREPOT, null, 3L))).isEqualTo(new Zone(ID_ZONE, "A", ID_ENTREPOT, null, 3L));
        assertThat(mapper.toAlerte(new AlertePaysDto("a1", "HAUTETEMPERATURE", "d", DATE, "zone"))).isEqualTo(new Alerte("a1", "HAUTETEMPERATURE", "d", DATE, "zone"));
    }

    @Test
    void reporting() {
        assertThat(mapper.toReportingGlobal(new ReportingGlobalPaysDto(10, 30.0, 80.0, 4))).isEqualTo(new ReportingGlobal(10, 30.0, 80.0, 4));
        assertThat(mapper.toReportingQuality(new ReportingQualityPaysDto(2, 80.0, 1))).isEqualTo(new ReportingQuality(2, 80.0, 1));
        assertThat(mapper.toReportingStock(new ReportingStockPaysDto(3, 12.5, 66.7, List.of(ID_LOT)))).isEqualTo(new ReportingStock(3, 12.5, 66.7, List.of(ID_LOT)));
        assertThat(mapper.toReportingStock(new ReportingStockPaysDto(0, 0, 0, null)).lotIdsUrgentFifo()).isNull();
    }

    @Test
    void resultats_d_operation() {
        assertThat(mapper.toResultatLot(new ResultatLotPaysDto(ID_LOT, "OK"))).isEqualTo(new ResultatLot(ID_LOT, "OK"));
        assertThat(mapper.toResultatSortieStock(new ResultatSortieStockPaysDto(ID_HISTORIQUE, "CREATED")))
                .isEqualTo(new ResultatSortieStock(ID_HISTORIQUE, "CREATED"));
    }

    @Test
    void nouveau_lot_vers_la_requete_du_pays() {
        assertThat(mapper.toDto(new NouveauLot("COMPLIANT", 120, 2, LocalDate.of(2026, 3, 15), ID_EXPLOITATION, ID_ZONE)))
                .isEqualTo(new NouveauLotPaysDto("COMPLIANT", 120, 2, LocalDate.of(2026, 3, 15), ID_EXPLOITATION, ID_ZONE));
    }
}
