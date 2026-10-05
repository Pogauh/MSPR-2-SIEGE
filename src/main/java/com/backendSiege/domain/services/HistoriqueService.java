package com.backendSiege.domain.services;

import com.backendSiege.domain.models.Historique;
import com.backendSiege.domain.models.TypeHistoriqueEnum;
import com.backendSiege.domain.ports.HistoriquePort;

import java.util.List;
import java.util.UUID;

/** Historique d'un lot, quel que soit le pays qui le possède. */
public class HistoriqueService {

    private final HistoriquePort historiquePort;
    private final PaysService paysService;

    public HistoriqueService(HistoriquePort historiquePort, PaysService paysService) {
        this.historiquePort = historiquePort;
        this.paysService = paysService;
    }

    /** @param type filtre sur le type d'événement, {@code null} pour tous */
    public List<Historique> findAllHistoriqueForLot(UUID idLot, TypeHistoriqueEnum type) {
        return historiquePort.findHistorique(paysService.codes(), idLot, type);
    }
}
