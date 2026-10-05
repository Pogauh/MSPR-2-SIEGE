package com.backendSiege.domain.services;

import com.backendSiege.domain.models.Lot;
import com.backendSiege.domain.models.MesureLot;
import com.backendSiege.domain.models.NouveauLot;
import com.backendSiege.domain.models.ResultatLot;
import com.backendSiege.domain.models.ResultatSortieStock;
import com.backendSiege.domain.ports.InventairePort;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Inventaire du siège : lots, mouvements de stock et mesures des lots, agrégés sur les backends des pays. */
public class InventaireService {

    private final InventairePort inventairePort;
    private final PaysService paysService;

    public InventaireService(InventairePort inventairePort, PaysService paysService) {
        this.inventairePort = inventairePort;
        this.paysService = paysService;
    }

    /** Quantité totale en stock dans tous les pays. */
    public int getQuantiteTotale() {
        return inventairePort.findQuantitesEnStock(paysService.codes()).stream().mapToInt(Integer::intValue).sum();
    }

    public Optional<Lot> getLotById(UUID idLot) {
        return inventairePort.findLot(paysService.codes(), idLot);
    }

    /**
     * Les lots du pays désigné, ou de tous les pays si la saisie est absente, vide ou inconnue,
     * du plus anciennement stocké au plus récent (les lots sans date de stockage en dernier).
     */
    public List<Lot> getAllLots(String pays) {
        return inventairePort.findLots(paysService.codesOuTous(pays)).stream()
                .sorted(Comparator.comparing(Lot::dateStockage, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<MesureLot> getAllMesuresByLotId(UUID idLot) {
        return inventairePort.findMesuresDuLot(paysService.codes(), idLot);
    }

    /**
     * Le lot à expédier en premier (FIFO) : le plus ancien du pays désigné, ou de tous les pays si la saisie est absente ou vide.
     * Un pays désigné mais inconnu n'a pas de lot.
     */
    public Optional<Lot> getOldestLot(String pays) {
        if (pays != null && !pays.isBlank()) {
            return paysService.resoudre(pays)
                    .flatMap(code -> inventairePort.findLotsLesPlusAnciens(List.of(code)).stream().findFirst());
        }
        return inventairePort.findLotsLesPlusAnciens(paysService.codes()).stream().reduce(Lot::lePlusAncien);
    }

    /** @return le résultat donné par le pays, vide si le pays est inconnu ou n'a pas pu traiter la demande */
    public Optional<ResultatLot> createLot(String pays, NouveauLot lot) {
        return paysService.resoudre(pays).flatMap(code -> inventairePort.createLot(code, lot));
    }

    /** @return le résultat donné par le pays, vide si le pays est inconnu ou n'a pas pu traiter la demande */
    public Optional<ResultatLot> updateStatusLot(String pays, UUID idLot, String status) {
        return paysService.resoudre(pays).flatMap(code -> inventairePort.updateStatus(code, idLot, status));
    }

    /** @return le résultat donné par le pays, vide si le pays est inconnu */
    public Optional<ResultatLot> updateEmplacementLot(String pays, UUID idLot, UUID idZone) {
        return paysService.resoudre(pays).flatMap(code -> inventairePort.updateEmplacement(code, idLot, idZone));
    }

    /** @return le résultat donné par le pays, vide si le pays est inconnu */
    public Optional<ResultatSortieStock> stockOut(String pays, UUID idLot) {
        return paysService.resoudre(pays).flatMap(code -> inventairePort.stockOut(code, idLot));
    }
}
