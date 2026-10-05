package com.backendSiege.domain.ports;

import com.backendSiege.domain.models.Alerte;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Alertes, telles que les connaissent les backends des pays. */
public interface AlertePort {

    /**
     * Les alertes des capteurs d'un entrepôt dans les pays interrogés (en parallèle) ; un pays en erreur ou injoignable est ignoré.
     *
     * @param debut début de la période, {@code null} pour ne pas la borner
     * @param fin   fin de la période, {@code null} pour ne pas la borner
     */
    List<Alerte> findAlertes(Collection<String> codesPays, UUID idEntrepot, LocalDateTime debut, LocalDateTime fin);
}
