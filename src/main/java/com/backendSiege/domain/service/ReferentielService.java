package com.backendSiege.domain.service;

import com.backendSiege.domain.model.Entrepot;
import com.backendSiege.domain.model.Exploitation;
import com.backendSiege.domain.model.ZoneResponse;
import com.backendSiege.domain.port.ReferentielPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class ReferentielService {

    private final ReferentielPort referentielPort;

    public List<Exploitation> getAllExploitationsByPays(String pays) {
        return referentielPort.findAllExploitationsByPays(pays);
    }

    public List<Entrepot> getAllEntrepotsByPays(String pays) {
        return referentielPort.findAllEntrepotsByPays(pays);
    }

    public List<ZoneResponse> findAllZonesByEntrepot(String pays, UUID entrepotId) {
        return referentielPort.findAllZonesByEntrepot(pays, entrepotId);
    }
}