package com.backendSiege.domain.service;

import com.backendSiege.domain.model.Historique;
import com.backendSiege.domain.model.TypeHistoriqueEnum;
import com.backendSiege.domain.port.HistoriquePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class HistoriqueService {

    private final HistoriquePort historiquePort;

    public List<Historique> findAllHistoriqueForLot(UUID idLot, TypeHistoriqueEnum type) {
        String typeString = (type != null) ? type.toString() : null;
        return historiquePort.findHistoriqueByLotId(idLot, typeString);
    }
}
