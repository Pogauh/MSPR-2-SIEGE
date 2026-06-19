package com.backendSiege.domain.service;

import com.backendSiege.api.dto.LotRequestDto;
import com.backendSiege.api.dto.LotStatusResponseDto;
import com.backendSiege.api.dto.MesureLotResponseDto;
import com.backendSiege.domain.port.InventairePort;
import com.backendSiege.api.dto.LotResponseDto;
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

    public LotResponseDto getLotById(UUID id) {
        return inventairePort.findLotById(id);
    }

    public List<LotResponseDto> getAllLots() {
        return inventairePort.findAllLots();
    }

    public List<MesureLotResponseDto> getAllMesuresByLotId(UUID id) {
        return inventairePort.findAllMesuresByLotId(id);
    }

    public LotResponseDto getOldestLotById(String pays) {
        return inventairePort.findOldestLotById(pays);
    }
    public LotStatusResponseDto createLot(LotRequestDto lot, String pays) {
        return inventairePort.createLot(lot, pays);
    }
}
