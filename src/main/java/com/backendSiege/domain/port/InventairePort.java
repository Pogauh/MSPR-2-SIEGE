package com.backendSiege.domain.port;

import com.backendSiege.api.dto.LotRequestDto;
import com.backendSiege.api.dto.LotResponseDto;
import com.backendSiege.api.dto.LotStatusResponseDto;
import com.backendSiege.api.dto.MesureLotResponseDto;

import java.util.UUID;
import java.util.List;

public interface InventairePort {

    Integer findQuantiteTotale();
    
    LotResponseDto findLotById(UUID id);

    List<LotResponseDto> findAllLots();

    List<MesureLotResponseDto> findAllMesuresByLotId(UUID id);

    LotResponseDto findOldestLotById(String pays);

    LotStatusResponseDto createLot(LotRequestDto lot, String pays);
}
