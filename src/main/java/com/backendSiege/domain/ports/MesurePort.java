package com.backendSiege.domain.ports;

import com.backendSiege.domain.models.CapteurSummary;
import com.backendSiege.domain.models.Mesure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Mesures des capteurs, telles que les connaissent les backends des pays. Le pays est désigné par son code normalisé.
 * Si le pays est en erreur ou injoignable, les lectures renvoient un résultat vide.
 */
public interface MesurePort {

    Optional<Mesure> findMesure(String codePays, UUID idMesure);

    List<Mesure> findMesuresDeLaZone(String codePays, UUID idZone, LocalDateTime debut, LocalDateTime fin);

    Optional<Mesure> findDerniereMesureDeLaZone(String codePays, UUID idZone);

    List<CapteurSummary> findResumeCapteurs(String codePays, UUID idEntrepot);
}
