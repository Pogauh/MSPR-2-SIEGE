package com.backendSiege.domain.models;

import java.time.LocalDateTime;

/** Mesure relevée dans la zone d'un lot pendant son séjour. */
public record MesureLot(Double temperature, Double humidite, LocalDateTime dateMesure) {
}
