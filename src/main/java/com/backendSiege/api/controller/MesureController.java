package com.backendSiege.api.controller;

import com.backendSiege.api.dto.CapteurSummaryResponseDto;
import com.backendSiege.api.dto.MesureResponseDto;
import com.backendSiege.domain.service.MesureService;
import com.backendSiege.infrastructure.mapper.CapteurMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class MesureController {

    private final MesureService mesureService;
    private final CapteurMapper capteurMapper;

    public MesureController(MesureService mesureService, CapteurMapper capteurMapper) {
        this.mesureService = mesureService;
        this.capteurMapper = capteurMapper;
    }

    @GetMapping("api/mesures/{mesureId}")
    public ResponseEntity<MesureResponseDto> getMesureById(@RequestParam String pays, @PathVariable UUID mesureId) {
        MesureResponseDto mesures = mesureService.getMesureById(pays, mesureId);
        return ResponseEntity.ok(mesures);
    }

    @GetMapping("api/capteurs/{idCapteur}/mesures")
    public ResponseEntity<List<MesureResponseDto>> getMesuresByCapteurId(@RequestParam String pays, @PathVariable UUID idCapteur) {
        List<MesureResponseDto> mesures = mesureService.getMesuresByCapteurId(pays, idCapteur);
        return ResponseEntity.ok(mesures);
    }

    @GetMapping("/warehouses/{idEntrepot}/readings/summary")
    public ResponseEntity<List<CapteurSummaryResponseDto>> getCapteurSummaryByEntrepotId(@RequestParam String pays, @PathVariable UUID idEntrepot) {
        try {
            return ResponseEntity.ok(
                    mesureService.getCapteurSummaryByEntrepotId(pays, idEntrepot)
                            .stream()
                            .map(capteurMapper::toResponse)
                            .toList()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
