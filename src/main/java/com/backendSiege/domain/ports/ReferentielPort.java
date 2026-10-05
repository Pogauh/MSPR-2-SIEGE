package com.backendSiege.domain.ports;

import com.backendSiege.domain.models.Entrepot;
import com.backendSiege.domain.models.Exploitation;
import com.backendSiege.domain.models.Zone;

import java.util.List;
import java.util.UUID;

/**
 * Référentiel d'un pays (exploitations, entrepôts, zones). Le pays est désigné par son code normalisé.
 * Chaque méthode lève {@link com.backendSiege.domain.exceptions.PaysIndisponibleException} si le pays est en erreur ou injoignable.
 */
public interface ReferentielPort {

    List<Exploitation> findExploitations(String codePays);

    List<Entrepot> findEntrepots(String codePays);

    List<Zone> findZones(String codePays, UUID idEntrepot);
}
