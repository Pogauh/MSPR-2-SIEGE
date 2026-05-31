package com.backendSiege.domain.port;

import com.backendSiege.api.dto.LotResponseDTO;
import java.util.UUID;
import java.util.List;

public interface InventairePort {

    Integer findQuantiteTotale();
    
    LotResponseDTO findLotById(UUID id);

    List<LotResponseDTO> findAllLots();
}
