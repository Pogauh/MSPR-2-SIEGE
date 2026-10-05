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
import org.mapstruct.Mapper;

/** Traduction entre le contrat des API REST des backends pays et le domaine du siège. */
@Mapper(config = PaysMapperConfig.class)
public interface PaysMapper {

    Lot toLot(LotPaysDto dto);

    Historique toHistorique(HistoriquePaysDto dto);

    MesureLot toMesureLot(MesureLotPaysDto dto);

    Mesure toMesure(MesurePaysDto dto);

    CapteurSummary toCapteurSummary(CapteurSummaryPaysDto dto);

    Entrepot toEntrepot(EntrepotPaysDto dto);

    Exploitation toExploitation(ExploitationPaysDto dto);

    Zone toZone(ZonePaysDto dto);

    Alerte toAlerte(AlertePaysDto dto);

    ReportingGlobal toReportingGlobal(ReportingGlobalPaysDto dto);

    ReportingQuality toReportingQuality(ReportingQualityPaysDto dto);

    ReportingStock toReportingStock(ReportingStockPaysDto dto);

    ResultatLot toResultatLot(ResultatLotPaysDto dto);

    ResultatSortieStock toResultatSortieStock(ResultatSortieStockPaysDto dto);

    NouveauLotPaysDto toDto(NouveauLot lot);
}
