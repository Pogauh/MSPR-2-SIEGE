package com.backendSiege.domain.port;

import com.backendSiege.domain.model.Historique;

import java.util.List;
import java.util.UUID;

public interface HistoriquePort {

    List<Historique> findHistoriqueByLotId(UUID idLot, String type);
}
