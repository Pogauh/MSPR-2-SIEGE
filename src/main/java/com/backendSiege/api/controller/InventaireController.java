package com.backendSiege.api.controller;

import com.backendSiege.api.dto.*;
import com.backendSiege.domain.service.InventaireService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
public class InventaireController {

    private final InventaireService inventaireService;

    public InventaireController(InventaireService inventaireService) {
        this.inventaireService = inventaireService;
    }

    @GetMapping("api/lot/total")
    public ResponseEntity<Integer> getQuantiteTotale() {
        Integer volume = inventaireService.getQuantiteTotale();
        if (volume == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(volume);
    }

    @GetMapping("/lot/{id}")
    public ResponseEntity<LotResponseDto> getLotById(@PathVariable UUID id) {
        LotResponseDto lot = inventaireService.getLotById(id);
        if (lot == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lot);
    }

    @GetMapping("/lots")
    public ResponseEntity<List<LotResponseDto>> getAllLots() {
        List<LotResponseDto> lots = inventaireService.getAllLots();
        return ResponseEntity.ok(lots);
    }

    @GetMapping("/lot/{idLot}/mesures")
    public ResponseEntity<List<MesureLotResponseDto>> getAllLots(@PathVariable UUID idLot) {
        List<MesureLotResponseDto> lots = inventaireService.getAllMesuresByLotId(idLot);
        return ResponseEntity.ok(lots);
    }

    @GetMapping("/lot/oldest")
    public ResponseEntity<LotResponseDto> getOldestLotById(@RequestParam(value = "pays", required = false) String pays) {
        LotResponseDto lot = inventaireService.getOldestLotById(pays);
        if (lot == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lot);
    }

    @PostMapping("/lot")
    public ResponseEntity<LotStatusResponseDto> createLot(@RequestBody LotRequestDto lot, @RequestParam String pays) {
        // Call the service to create the lot
        LotStatusResponseDto createdLot = inventaireService.createLot(lot,pays);
        return ResponseEntity.ok(createdLot);
    }

    @PutMapping("/lot/{idLot}/status")
    public ResponseEntity<LotStatusResponseDto> updateStatusLot(@PathVariable("idLot") UUID idLot, @RequestParam String pays, @RequestBody LotStatusRequestDto request) {

        LotStatusResponseDto updatedLot = inventaireService.updateStatusLot(idLot, pays, request);
        return ResponseEntity.ok(updatedLot);
    }

    @PutMapping("/lot/{idLot}/zones")
    public ResponseEntity<LotStatusResponseDto> updateEmplacementLot(@PathVariable("idLot") UUID idLot, @RequestParam String pays, @RequestBody LotEmplacementRequestDto request) {

        LotStatusResponseDto updatedLot = inventaireService.updateEmplacementLot(idLot, pays, request);
        return ResponseEntity.ok(updatedLot);
    }

    @PostMapping("/mouvements/stockout")
    public ResponseEntity<HistoriqueStatusDto> stockOutLot(@RequestParam("idLot") UUID idLot, @RequestParam String pays) {
        HistoriqueStatusDto updatedLot = inventaireService.stockOut(idLot, pays);
        return ResponseEntity.ok(updatedLot);
    }
}