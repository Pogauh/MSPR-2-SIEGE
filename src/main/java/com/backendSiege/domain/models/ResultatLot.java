package com.backendSiege.domain.models;

import java.util.UUID;

/** Résultat d'une opération sur un lot (création, changement de statut, transfert) : l'identifiant du lot et l'issue donnée par le pays. */
public record ResultatLot(UUID idLot, String status) {
}
