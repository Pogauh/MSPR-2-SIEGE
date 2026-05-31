package com.backendSiege.api.controller;

import com.backendSiege.api.dto.LotResponseDTO;
import com.backendSiege.api.dto.MesureResponseDTO;
import com.backendSiege.domain.service.MesureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MesureController {

    private final MesureService mesureService;

    public MesureController(MesureService mesureService) {
        this.mesureService = mesureService;
    }

    @GetMapping("/mesures/{id}")
    public ResponseEntity<MesureResponseDTO> getMesureById(@PathVariable UUID id) {
        MesureResponseDTO mesures = mesureService.getMesureById(id);
        return ResponseEntity.ok(mesures);
    }
}
