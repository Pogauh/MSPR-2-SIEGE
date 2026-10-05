package com.backendSiege.api.controller;

import com.backendSiege.api.dto.AlerteDto;
import com.backendSiege.api.mapper.AlerteMapper;
import com.backendSiege.domain.services.AlerteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
public class AlerteController {

    private final AlerteService alerteService;
    private final AlerteMapper alerteMapper;

    public AlerteController(AlerteService alerteService, AlerteMapper alerteMapper) {
        this.alerteService = alerteService;
        this.alerteMapper = alerteMapper;
    }

    /** Les alertes d'un entrepôt, sur une période facultative. Un éventuel paramètre {@code pays} est sans effet : l'entrepôt suffit. */
    @GetMapping("/alertes")
    public List<AlerteDto> getAlertes(@RequestParam UUID idEntrepot,
                                      @RequestParam(value = "startDate", required = false) LocalDateTime startDate,
                                      @RequestParam(value = "endDate", required = false) LocalDateTime endDate) {
        return alerteService.getAllAlertesByEntrepotId(idEntrepot, startDate, endDate).stream()
                .map(alerteMapper::toDto)
                .toList();
    }
}
