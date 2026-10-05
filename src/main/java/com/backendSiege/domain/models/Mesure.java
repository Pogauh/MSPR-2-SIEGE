package com.backendSiege.domain.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** Mesure de température et d'humidité d'un capteur. */
public record Mesure(UUID idMesure, Double temperature, Double humidite, LocalDateTime dateMesure, UUID idCapteur) {
}
