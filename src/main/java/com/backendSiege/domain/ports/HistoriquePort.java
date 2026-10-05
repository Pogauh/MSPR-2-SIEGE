package com.backendSiege.domain.ports;

import com.backendSiege.domain.models.Historique;
import com.backendSiege.domain.models.TypeHistoriqueEnum;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Historique des lots, tel que le connaissent les backends des pays. */
public interface HistoriquePort {

    /**
     * L'historique du lot dans les pays interrogés (en parallèle) ; un pays en erreur ou injoignable est ignoré.
     *
     * @param type filtre sur le type d'événement, {@code null} pour tous
     */
    List<Historique> findHistorique(Collection<String> codesPays, UUID idLot, TypeHistoriqueEnum type);
}
