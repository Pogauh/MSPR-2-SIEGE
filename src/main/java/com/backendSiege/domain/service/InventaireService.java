package com.backendSiege.domain.service;

import com.backendSiege.domain.port.InventairePort;
import com.backendSiege.api.dto.LotResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class InventaireService {

    private final InventairePort inventairePort;

    public Integer getQuantiteTotale() {
        return inventairePort.findQuantiteTotale();
    }

    public LotResponseDTO getLotById(UUID id) {
        return inventairePort.findLotById(id);
    }

    public List<LotResponseDTO> getAllLots() {
        return inventairePort.findAllLots();
    }
}
