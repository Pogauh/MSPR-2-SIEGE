package com.backendSiege.domain.models;

import java.time.LocalDateTime;

/** Alerte levée par un backend pays (dépassement de seuil d'un capteur, lot expiré…). */
public record Alerte(String idAlerte, String typeAlerte, String description, LocalDateTime dateEmission, String idZone) {
}
