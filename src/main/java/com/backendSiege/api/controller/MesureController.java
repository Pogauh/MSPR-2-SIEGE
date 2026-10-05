package com.backendSiege.api.controller;

import com.backendSiege.api.dto.CapteurSummaryResponseDto;
import com.backendSiege.api.dto.MesureResponseDto;
import com.backendSiege.api.mapper.CapteurSummaryMapper;
import com.backendSiege.api.mapper.MesureMapper;
import com.backendSiege.domain.services.MesureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Mesures des capteurs d'un pays. Une mesure introuvable ou un pays qui ne répond pas donne un {@code 200} sans corps. */
@RestController
public class MesureController {

    private final MesureService mesureService;
    private final MesureMapper mesureMapper;
    private final CapteurSummaryMapper capteurSummaryMapper;

    public MesureController(MesureService mesureService, MesureMapper mesureMapper, CapteurSummaryMapper capteurSummaryMapper) {
        this.mesureService = mesureService;
        this.mesureMapper = mesureMapper;
        this.capteurSummaryMapper = capteurSummaryMapper;
    }

    @GetMapping("/api/mesures/{mesureId}")
    public ResponseEntity<MesureResponseDto> getMesureById(@RequestParam String pays, @PathVariable UUID mesureId) {
        return ResponseEntity.ok(mesureService.getMesureById(pays, mesureId).map(mesureMapper::toResponse).orElse(null));
    }

    /** Dernière mesure et moyennes sur 24 h de chaque zone d'un entrepôt. */
    @GetMapping("/warehouses/{idEntrepot}/readings/summary")
    public ResponseEntity<List<CapteurSummaryResponseDto>> getCapteurSummaryByEntrepotId(@RequestParam String pays,
                                                                                        @PathVariable UUID idEntrepot) {
        return ResponseEntity.ok(mesureService.getCapteurSummaryByEntrepotId(pays, idEntrepot).stream()
                .map(capteurSummaryMapper::toResponse)
                .toList());
    }

    /** Les mesures d'une zone sur une période. Les dates sont de la forme {@code 2024-06-05T18:46}. */
    @GetMapping("/readings/{idZone}/")
    public ResponseEntity<List<MesureResponseDto>> getMesuresByZoneId(@RequestParam String pays,
                                                                      @PathVariable UUID idZone,
                                                                      @RequestParam("startDate") LocalDateTime startDate,
                                                                      @RequestParam("endDate") LocalDateTime endDate) {
        return ResponseEntity.ok(mesureService.getMesuresByZoneId(pays, idZone, startDate, endDate).stream()
                .map(mesureMapper::toResponse)
                .toList());
    }

    @GetMapping("/zones/{idZone}/readings/latest")
    public ResponseEntity<MesureResponseDto> getDerniereMesureByZoneId(@RequestParam String pays, @PathVariable UUID idZone) {
        return ResponseEntity.ok(mesureService.getLastMesureByZoneId(pays, idZone).map(mesureMapper::toResponse).orElse(null));
    }
}
