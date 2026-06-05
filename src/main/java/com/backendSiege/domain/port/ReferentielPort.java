package com.backendSiege.domain.port;

import com.backendSiege.domain.model.Entrepot;
import com.backendSiege.domain.model.Exploitation;

import java.util.List;

public interface ReferentielPort {
    List<Exploitation> findAllExploitationsByPays(String pays);

    List<Entrepot> findAllEntrepotsByPays(String pays);
}
