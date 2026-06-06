package com.backendSiege.api.controller;

import com.backendSiege.domain.service.InventaireService;
import com.backendSiege.api.dto.LotResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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
}