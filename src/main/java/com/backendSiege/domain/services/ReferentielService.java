package com.backendSiege.domain.services;

import com.backendSiege.domain.models.Entrepot;
import com.backendSiege.domain.models.Exploitation;
import com.backendSiege.domain.models.Pays;
import com.backendSiege.domain.models.Zone;
import com.backendSiege.domain.ports.ReferentielPort;

import java.util.List;
import java.util.UUID;

/**
 * Référentiel du siège : pays, exploitations, entrepôts et zones. Un pays inconnu n'a ni exploitation, ni entrepôt, ni zone.
 *
 * @see ReferentielPort pour le cas d'un pays qui n'a pas pu répondre
 */
public class ReferentielService {

    private final ReferentielPort referentielPort;
    private final PaysService paysService;

    public ReferentielService(ReferentielPort referentielPort, PaysService paysService) {
        this.referentielPort = referentielPort;
        this.paysService = paysService;
    }

    public List<Pays> getAllPays() {
        return paysService.findAll();
    }

    public List<Exploitation> getAllExploitationsByPays(String pays) {
        return paysService.resoudre(pays).map(referentielPort::findExploitations).orElseGet(List::of);
    }

    public List<Entrepot> getAllEntrepotsByPays(String pays) {
        return paysService.resoudre(pays).map(referentielPort::findEntrepots).orElseGet(List::of);
    }

    public List<Zone> getAllZonesByEntrepot(String pays, UUID idEntrepot) {
        return paysService.resoudre(pays).map(code -> referentielPort.findZones(code, idEntrepot)).orElseGet(List::of);
    }
}
