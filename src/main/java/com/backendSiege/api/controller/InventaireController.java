package com.backendSiege.api.controller;

import com.backendSiege.api.dto.InventaireDto;
import com.backendSiege.api.mapper.InventaireDtoMapper;
import com.backendSiege.domain.service.InventaireService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InventaireController {

    private final InventaireService inventaireService;
    private final InventaireDtoMapper inventaireDtoMapper;

    public InventaireController(InventaireService inventaireService, InventaireDtoMapper inventaireDtoMapper) {
        this.inventaireService = inventaireService;
        this.inventaireDtoMapper = inventaireDtoMapper;
    }

    @GetMapping("/inventaires/mondial")
    public ResponseEntity<List<InventaireDto>> getInventaireMondial() {
        try {
            List<InventaireDto> inventaires = inventaireDtoMapper.toDto(inventaireService.getInventaireMondial());
            if (inventaires.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(inventaires);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

