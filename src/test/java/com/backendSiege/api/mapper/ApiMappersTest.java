package com.backendSiege.api.mapper;

import com.backendSiege.api.dto.AlerteDto;
import com.backendSiege.api.dto.CapteurSummaryResponseDto;
import com.backendSiege.api.dto.EntrepotDto;
import com.backendSiege.api.dto.ExploitationDto;
import com.backendSiege.api.dto.HistoriqueResponseDto;
import com.backendSiege.api.dto.HistoriqueStatusDto;
import com.backendSiege.api.dto.LotRequestDto;
import com.backendSiege.api.dto.LotResponseDto;
import com.backendSiege.api.dto.LotStatusResponseDto;
import com.backendSiege.api.dto.MesureLotResponseDto;
import com.backendSiege.api.dto.MesureResponseDto;
import com.backendSiege.api.dto.PaysDto;
import com.backendSiege.api.dto.ReportingGlobalDto;
import com.backendSiege.api.dto.ReportingQualityDto;
import com.backendSiege.api.dto.ReportingStockDto;
import com.backendSiege.api.dto.ZoneResponseDto;
import com.backendSiege.api.dto.auth.MeResponseDTO;
import com.backendSiege.domain.models.Alerte;
import com.backendSiege.domain.models.CapteurSummary;
import com.backendSiege.domain.models.Entrepot;
import com.backendSiege.domain.models.Exploitation;
import com.backendSiege.domain.models.Historique;
import com.backendSiege.domain.models.Lot;
import com.backendSiege.domain.models.Mesure;
import com.backendSiege.domain.models.MesureLot;
import com.backendSiege.domain.models.NouveauLot;
import com.backendSiege.domain.models.Pays;
import com.backendSiege.domain.models.ReportingGlobal;
import com.backendSiege.domain.models.ReportingQuality;
import com.backendSiege.domain.models.ReportingStock;
import com.backendSiege.domain.models.ResultatLot;
import com.backendSiege.domain.models.ResultatSortieStock;
import com.backendSiege.domain.models.Role;
import com.backendSiege.domain.models.Tolerances;
import com.backendSiege.domain.models.User;
import com.backendSiege.domain.models.Zone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Traduction du domaine vers les DTO de l'API (et inversement pour les requêtes) : chaque champ est reporté tel quel. */
@SpringJUnitConfig({AlerteMapperImpl.class, CapteurSummaryMapperImpl.class, HistoriqueMapperImpl.class, LotMapperImpl.class,
        MesureMapperImpl.class, ReferentielMapperImpl.class, ReportingMapperImpl.class, UserMapperImpl.class})
class ApiMappersTest {

    private static final UUID ID_LOT = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ID_ZONE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ID_HISTORIQUE = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ID_ENTREPOT = UUID.fromString("770e8400-e29b-41d4-a716-446655440011");
    private static final UUID ID_EXPLOITATION = UUID.fromString("aa0e8400-e29b-41d4-a716-446655440022");
    private static final UUID ID_CAPTEUR = UUID.fromString("b793e242-053a-4def-9cd8-96c990a586c9");
    private static final LocalDateTime DATE = LocalDateTime.of(2026, 1, 12, 10, 0);

    @Autowired
    AlerteMapper alerteMapper;
    @Autowired
    CapteurSummaryMapper capteurSummaryMapper;
    @Autowired
    HistoriqueMapper historiqueMapper;
    @Autowired
    LotMapper lotMapper;
    @Autowired
    MesureMapper mesureMapper;
    @Autowired
    ReferentielMapper referentielMapper;
    @Autowired
    ReportingMapper reportingMapper;
    @Autowired
    UserMapper userMapper;

    @Test
    void alerte() {
        assertThat(alerteMapper.toDto(new Alerte("a1", "HAUTETEMPERATURE", "Trop chaud", DATE, ID_ZONE.toString())))
                .isEqualTo(new AlerteDto("a1", "HAUTETEMPERATURE", "Trop chaud", DATE, ID_ZONE.toString()));
    }

    @Test
    void resume_de_capteur_conserve_les_decimales() {
        CapteurSummaryResponseDto dto = capteurSummaryMapper.toResponse(new CapteurSummary(ID_ZONE, "A", new BigDecimal("27.50"),
                new BigDecimal("56.20"), new BigDecimal("27.125"), new BigDecimal("56")));

        assertThat(dto).isEqualTo(new CapteurSummaryResponseDto(ID_ZONE, "A", new BigDecimal("27.50"), new BigDecimal("56.20"),
                new BigDecimal("27.125"), new BigDecimal("56")));
        assertThat(dto.lastTemp().scale()).isEqualTo(2);
    }

    @Test
    void historique() {
        assertThat(historiqueMapper.toResponse(new Historique(ID_HISTORIQUE, "TRANSFERT", DATE, null, ID_ZONE, "Transfert")))
                .isEqualTo(new HistoriqueResponseDto(ID_HISTORIQUE, "TRANSFERT", DATE, null, ID_ZONE, "Transfert"));
    }

    @Test
    void lot_avec_son_historique() {
        Lot lot = new Lot(ID_LOT, "COMPLIANT", 120, 2, null, DATE, LocalDate.of(2025, 12, 1), ID_ZONE.toString(), ID_EXPLOITATION,
                ID_ENTREPOT, List.of(new Historique(ID_HISTORIQUE, "ALERTE", DATE, ID_ZONE, null, "Alerte")));

        assertThat(lotMapper.toResponse(lot)).isEqualTo(new LotResponseDto(ID_LOT, "COMPLIANT", 120, 2, null, DATE,
                LocalDate.of(2025, 12, 1), ID_ZONE.toString(), ID_EXPLOITATION, ID_ENTREPOT,
                List.of(new HistoriqueResponseDto(ID_HISTORIQUE, "ALERTE", DATE, ID_ZONE, null, "Alerte"))));
    }

    @Test
    void lot_sans_historique_garde_une_liste_nulle_et_non_vide() {
        Lot lot = new Lot(ID_LOT, null, null, null, null, null, null, null, null, null, null);

        LotResponseDto dto = lotMapper.toResponse(lot);

        assertThat(dto.historique()).isNull();
        assertThat(dto).isEqualTo(new LotResponseDto(ID_LOT, null, null, null, null, null, null, null, null, null, null));
    }

    @Test
    void requete_de_creation_de_lot() {
        LotRequestDto requete = new LotRequestDto("COMPLIANT", 120, 2, LocalDate.of(2026, 3, 15), ID_EXPLOITATION, ID_ZONE);

        assertThat(lotMapper.toNouveauLot(requete))
                .isEqualTo(new NouveauLot("COMPLIANT", 120, 2, LocalDate.of(2026, 3, 15), ID_EXPLOITATION, ID_ZONE));
    }

    @Test
    void resultats_d_operation() {
        assertThat(lotMapper.toStatusResponse(new ResultatLot(ID_LOT, "OK"))).isEqualTo(new LotStatusResponseDto(ID_LOT, "OK"));
        assertThat(lotMapper.toSortieStockResponse(new ResultatSortieStock(ID_HISTORIQUE, "CREATED")))
                .isEqualTo(new HistoriqueStatusDto(ID_HISTORIQUE, "CREATED"));
    }

    @Test
    void mesure_de_lot() {
        assertThat(lotMapper.toMesureLotResponse(new MesureLot(27.5, null, DATE))).isEqualTo(new MesureLotResponseDto(27.5, null, DATE));
    }

    @Test
    void mesure() {
        assertThat(mesureMapper.toResponse(new Mesure(UUID.fromString("99999999-9999-9999-9999-999999999999"), 27.5, 56.2, DATE, ID_CAPTEUR)))
                .isEqualTo(new MesureResponseDto(UUID.fromString("99999999-9999-9999-9999-999999999999"), 27.5, 56.2, DATE, ID_CAPTEUR));
    }

    @Test
    void referentiel() {
        assertThat(referentielMapper.toDto(new Exploitation(ID_EXPLOITATION, "Ferme", "Rue 1")))
                .isEqualTo(new ExploitationDto(ID_EXPLOITATION, "Ferme", "Rue 1"));
        assertThat(referentielMapper.toDto(new Entrepot(ID_ENTREPOT, "Santos", "Principal")))
                .isEqualTo(new EntrepotDto(ID_ENTREPOT, "Santos", "Principal"));
        assertThat(referentielMapper.toResponse(new Zone(ID_ZONE, "A", ID_ENTREPOT, ID_CAPTEUR, 3L)))
                .isEqualTo(new ZoneResponseDto(ID_ZONE, "A", ID_ENTREPOT, ID_CAPTEUR, 3L));
    }

    @Test
    void pays_et_ses_tolerances() {
        assertThat(referentielMapper.toDto(new Pays("BRESIL", "http://bresil/api/", new Tolerances(29, 55, 3, 2))))
                .isEqualTo(new PaysDto("http://bresil/api/", new PaysDto.TolerancesDto(29, 55, 3, 2)));
        assertThat(referentielMapper.toDto(new Pays("BRESIL", "http://bresil/api/", null)))
                .isEqualTo(new PaysDto("http://bresil/api/", null));
    }

    @Test
    void reporting() {
        assertThat(reportingMapper.toDto(new ReportingGlobal(40, 45.0, 95.0, 10))).isEqualTo(new ReportingGlobalDto(40, 45.0, 95.0, 10));
        assertThat(reportingMapper.toDto(new ReportingQuality(5, 70.0, 3))).isEqualTo(new ReportingQualityDto(5, 70.0, 3));
        assertThat(reportingMapper.toDto(new ReportingStock(3, 12.5, 66.7, List.of(ID_LOT))))
                .isEqualTo(new ReportingStockDto(3, 12.5, 66.7, List.of(ID_LOT)));
        assertThat(reportingMapper.toDto(ReportingStock.vide())).isEqualTo(new ReportingStockDto(0, 0, 0, null));
    }

    @Test
    void utilisateur_sans_son_mot_de_passe() {
        MeResponseDTO me = userMapper.toMeResponse(new User(ID_LOT, "neo@siege.fr", "hache", Set.of(Role.ROLE_QUALITY), "colombie"));

        assertThat(me).isEqualTo(new MeResponseDTO(ID_LOT, "neo@siege.fr", Set.of(Role.ROLE_QUALITY), "colombie"));
    }
}
