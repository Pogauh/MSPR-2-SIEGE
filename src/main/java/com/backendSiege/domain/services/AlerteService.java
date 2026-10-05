package com.backendSiege.domain.services;

import com.backendSiege.domain.models.Alerte;
import com.backendSiege.domain.ports.AlertePort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Alertes d'un entrepôt. L'identifiant d'un entrepôt est unique : on interroge tous les pays, seul le sien répond. */
public class AlerteService {

    private final AlertePort alertePort;
    private final PaysService paysService;

    public AlerteService(AlertePort alertePort, PaysService paysService) {
        this.alertePort = alertePort;
        this.paysService = paysService;
    }

    /**
     * @param debut début de la période, {@code null} pour ne pas la borner
     * @param fin   fin de la période, {@code null} pour ne pas la borner
     */
    public List<Alerte> getAllAlertesByEntrepotId(UUID idEntrepot, LocalDateTime debut, LocalDateTime fin) {
        return alertePort.findAlertes(paysService.codes(), idEntrepot, debut, fin);
    }
}
