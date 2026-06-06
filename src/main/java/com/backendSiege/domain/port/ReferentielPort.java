package com.backendSiege.domain.port;

import com.backendSiege.domain.model.Entrepot;
import com.backendSiege.domain.model.Exploitation;
import com.backendSiege.domain.model.ZoneResponse;

import java.util.List;
import java.util.UUID;

public interface ReferentielPort {
    List<Exploitation> findAllExploitationsByPays(String pays);

    List<Entrepot> findAllEntrepotsByPays(String pays);

    List<ZoneResponse> findAllZonesByEntrepot(String pays, UUID entrepotId);
}
