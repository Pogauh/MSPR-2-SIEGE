package com.backendSiege.domain.models;

import java.util.UUID;

/** Zone d'un entrepôt, surveillée par un capteur, avec le nombre de lots qu'elle contient. */
public record Zone(UUID idZone, String nomZone, UUID idEntrepot, UUID idCapteur, Long lotCount) {
}
