package com.backendSiege.domain.models;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement de la vie d'un lot (transfert, alerte, expédition).
 *
 * @param type le type tel que renvoyé par le pays : il est relayé sans être contraint à {@link TypeHistoriqueEnum},
 *             pour qu'un type ajouté côté pays ne fasse pas disparaître le lot
 */
public record Historique(UUID idHistorique, String type, LocalDateTime dateEmission, UUID zonePrecedente,
                         UUID zoneActuelle, String detail) {
}
