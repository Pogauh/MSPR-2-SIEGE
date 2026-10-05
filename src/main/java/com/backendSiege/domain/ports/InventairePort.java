package com.backendSiege.domain.ports;

import com.backendSiege.domain.models.Lot;
import com.backendSiege.domain.models.MesureLot;
import com.backendSiege.domain.models.NouveauLot;
import com.backendSiege.domain.models.ResultatLot;
import com.backendSiege.domain.models.ResultatSortieStock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Lots et mouvements de stock, tels que les connaissent les backends des pays.
 * <p>
 * Les pays sont désignés par leur code normalisé (voir {@code PaysService}). Les lectures sur plusieurs pays les interrogent
 * en parallèle et sont tolérantes : un pays qui répond en erreur ou qui est injoignable est ignoré.
 */
public interface InventairePort {

    /** Quantité en stock de chaque pays interrogé ({@code 0} pour un pays qui n'a pas pu répondre). */
    List<Integer> findQuantitesEnStock(Collection<String> codesPays);

    /** Le lot, tel que le connaît le premier pays (dans l'ordre donné) qui le possède. */
    Optional<Lot> findLot(Collection<String> codesPays, UUID idLot);

    /** Les lots de tous les pays interrogés, pays par pays. */
    List<Lot> findLots(Collection<String> codesPays);

    /** Les mesures relevées dans les zones traversées par le lot, vide si aucun pays ne connaît le lot. */
    List<MesureLot> findMesuresDuLot(Collection<String> codesPays, UUID idLot);

    /** Le lot à expédier en premier (FIFO) de chaque pays interrogé qui en a un. */
    List<Lot> findLotsLesPlusAnciens(Collection<String> codesPays);

    /** @return le résultat donné par le pays, vide s'il a refusé la demande, n'a pas répondu ou est injoignable */
    Optional<ResultatLot> createLot(String codePays, NouveauLot lot);

    /** @return le résultat donné par le pays, vide s'il a refusé la demande, n'a pas répondu ou est injoignable */
    Optional<ResultatLot> updateStatus(String codePays, UUID idLot, String status);

    /**
     * @return le résultat donné par le pays, vide s'il n'a rien répondu
     * @throws com.backendSiege.domain.exceptions.PaysIndisponibleException si le pays refuse la demande ou est injoignable
     */
    Optional<ResultatLot> updateEmplacement(String codePays, UUID idLot, UUID idZone);

    /**
     * @return le résultat donné par le pays, vide s'il n'a rien répondu
     * @throws com.backendSiege.domain.exceptions.PaysIndisponibleException si le pays refuse la demande ou est injoignable
     */
    Optional<ResultatSortieStock> stockOut(String codePays, UUID idLot);
}
