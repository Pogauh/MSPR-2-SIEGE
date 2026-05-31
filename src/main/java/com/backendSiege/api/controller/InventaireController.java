package com.backendSiege.api.controller;

import com.backendSiege.domain.service.InventaireService;
import com.backendSiege.api.dto.LotResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api")
public class InventaireController {

    private final InventaireService inventaireService;

    public InventaireController(InventaireService inventaireService) {
        this.inventaireService = inventaireService;
    }

    @GetMapping("/inventaires/total")
    public ResponseEntity<Integer> getQuantiteTotale() {
        Integer volume = inventaireService.getQuantiteTotale();
        System.out.println("Volume total de l'inventaire mondial : " + volume);
        if (volume == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(volume);
    }

    @GetMapping("/lot/{id}")
    public ResponseEntity<LotResponseDTO> getLotById(@PathVariable UUID id) {
        LotResponseDTO lot = inventaireService.getLotById(id);
        if (lot == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lot);
    }

    @GetMapping("/lots")
    public ResponseEntity<List<LotResponseDTO>> getAllLots() {
        List<LotResponseDTO> lots = inventaireService.getAllLots();
        return ResponseEntity.ok(lots);
    }
}
