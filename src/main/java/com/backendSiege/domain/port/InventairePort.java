package com.backendSiege.domain.port;

import com.backendSiege.api.dto.LotResponseDto;
import java.util.UUID;
import java.util.List;

public interface InventairePort {

    Integer findQuantiteTotale();
    
    LotResponseDto findLotById(UUID id);

    List<LotResponseDto> findAllLots();
}
