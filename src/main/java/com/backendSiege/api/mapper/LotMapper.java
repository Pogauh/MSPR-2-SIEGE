package com.backendSiege.api.mapper;

import com.backendSiege.api.dto.HistoriqueStatusDto;
import com.backendSiege.api.dto.LotRequestDto;
import com.backendSiege.api.dto.LotResponseDto;
import com.backendSiege.api.dto.LotStatusResponseDto;
import com.backendSiege.api.dto.MesureLotResponseDto;
import com.backendSiege.domain.models.Lot;
import com.backendSiege.domain.models.MesureLot;
import com.backendSiege.domain.models.NouveauLot;
import com.backendSiege.domain.models.ResultatLot;
import com.backendSiege.domain.models.ResultatSortieStock;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class, uses = HistoriqueMapper.class)
public interface LotMapper {

    LotResponseDto toResponse(Lot lot);

    NouveauLot toNouveauLot(LotRequestDto request);

    LotStatusResponseDto toStatusResponse(ResultatLot resultat);

    HistoriqueStatusDto toSortieStockResponse(ResultatSortieStock resultat);

    MesureLotResponseDto toMesureLotResponse(MesureLot mesure);
}
