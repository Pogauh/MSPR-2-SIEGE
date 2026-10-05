package com.backendSiege.domain.models;

import java.time.LocalDate;
import java.util.UUID;

/** Demande de création d'un lot : l'exploitation et la zone ne sont désignées que par leur identifiant. */
public record NouveauLot(String status, Integer quantite, Integer caracteristique, LocalDate dateProduction,
                         UUID idExploitation, UUID idZone) {
}
