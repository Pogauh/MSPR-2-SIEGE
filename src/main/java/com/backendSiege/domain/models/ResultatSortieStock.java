package com.backendSiege.domain.models;

import java.util.UUID;

/** Résultat d'une sortie de stock : l'identifiant de l'événement d'historique créé et l'issue donnée par le pays. */
public record ResultatSortieStock(UUID idHistorique, String status) {
}
