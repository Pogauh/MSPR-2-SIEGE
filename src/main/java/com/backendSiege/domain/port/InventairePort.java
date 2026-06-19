package com.backendSiege.domain.port;

import com.backendSiege.api.dto.*;

import java.util.UUID;
import java.util.List;

public interface InventairePort {

    Integer findQuantiteTotale();
    
    LotResponseDto findLotById(UUID id);

    List<LotResponseDto> findAllLots(String pays);

    List<MesureLotResponseDto> findAllMesuresByLotId(UUID id);

    LotResponseDto findOldestLotById(String pays);

    LotStatusResponseDto createLot(LotRequestDto lot, String pays);

    LotStatusResponseDto updateStatusLot(UUID idLot, String pays, LotStatusRequestDto statusRequest);

    LotStatusResponseDto updateEmplacementLot(UUID idLot, String pays, LotEmplacementRequestDto zoneRequest);

    HistoriqueStatusDto stockOut(UUID idLot, String pays);
}
