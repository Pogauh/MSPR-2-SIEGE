package com.backendSiege.api.controller;

import com.backendSiege.api.dto.HistoriqueStatusDto;
import com.backendSiege.api.dto.LotEmplacementRequestDto;
import com.backendSiege.api.dto.LotRequestDto;
import com.backendSiege.api.dto.LotResponseDto;
import com.backendSiege.api.dto.LotStatusRequestDto;
import com.backendSiege.api.dto.LotStatusResponseDto;
import com.backendSiege.api.dto.MesureLotResponseDto;
import com.backendSiege.api.mapper.LotMapper;
import com.backendSiege.domain.services.InventaireService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Lots et mouvements de stock de tous les pays.
 * <p>
 * Pour les écritures (création, statut, transfert, sortie de stock), le pays est obligatoire ; si le pays est inconnu ou ne
 * donne pas de réponse exploitable, la réponse est un {@code 200} sans corps (comportement historique).
 */
@RestController
public class InventaireController {

    private final InventaireService inventaireService;
    private final LotMapper lotMapper;

    public InventaireController(InventaireService inventaireService, LotMapper lotMapper) {
        this.inventaireService = inventaireService;
        this.lotMapper = lotMapper;
    }

    /** Quantité totale en stock ; {@code 404} s'il n'y en a aucune. */
    @GetMapping("/api/lot/total")
    public ResponseEntity<Integer> getQuantiteTotale() {
        int volume = inventaireService.getQuantiteTotale();
        if (volume == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(volume);
    }

    @GetMapping("/lot/{id}")
    public ResponseEntity<LotResponseDto> getLotById(@PathVariable UUID id) {
        return inventaireService.getLotById(id)
                .map(lotMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Les lots d'un pays (tous les pays si {@code pays} est absent ou inconnu), du plus ancien au plus récent. */
    @GetMapping("/lots")
    public ResponseEntity<List<LotResponseDto>> getAllLots(@RequestParam(required = false) String pays) {
        return ResponseEntity.ok(inventaireService.getAllLots(pays).stream().map(lotMapper::toResponse).toList());
    }

    @GetMapping("/lot/{idLot}/mesures")
    public ResponseEntity<List<MesureLotResponseDto>> getAllMesuresByLotId(@PathVariable UUID idLot) {
        return ResponseEntity.ok(inventaireService.getAllMesuresByLotId(idLot).stream().map(lotMapper::toMesureLotResponse).toList());
    }

    /** Le lot à expédier en premier (FIFO). */
    @GetMapping("/lot/oldest")
    public ResponseEntity<LotResponseDto> getOldestLot(@RequestParam(value = "pays", required = false) String pays) {
        return inventaireService.getOldestLot(pays)
                .map(lotMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/lot")
    public ResponseEntity<LotStatusResponseDto> createLot(@RequestBody LotRequestDto lot, @RequestParam String pays) {
        return ResponseEntity.ok(inventaireService.createLot(pays, lotMapper.toNouveauLot(lot)).map(lotMapper::toStatusResponse).orElse(null));
    }

    @PutMapping("/lot/{idLot}/status")
    public ResponseEntity<LotStatusResponseDto> updateStatusLot(@PathVariable("idLot") UUID idLot, @RequestParam String pays,
                                                                @RequestBody LotStatusRequestDto request) {
        return ResponseEntity.ok(inventaireService.updateStatusLot(pays, idLot, request.status()).map(lotMapper::toStatusResponse).orElse(null));
    }

    @PutMapping("/lot/{idLot}/zones")
    public ResponseEntity<LotStatusResponseDto> updateEmplacementLot(@PathVariable("idLot") UUID idLot, @RequestParam String pays,
                                                                     @RequestBody LotEmplacementRequestDto request) {
        return ResponseEntity.ok(inventaireService.updateEmplacementLot(pays, idLot, request.idZone()).map(lotMapper::toStatusResponse).orElse(null));
    }

    @PostMapping("/mouvements/stockout")
    public ResponseEntity<HistoriqueStatusDto> stockOutLot(@RequestParam("idLot") UUID idLot, @RequestParam String pays) {
        return ResponseEntity.ok(inventaireService.stockOut(pays, idLot).map(lotMapper::toSortieStockResponse).orElse(null));
    }
}
