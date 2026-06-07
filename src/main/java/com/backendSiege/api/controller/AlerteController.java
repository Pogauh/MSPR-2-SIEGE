package com.backendSiege.api.controller;

import com.backendSiege.api.dto.AlerteDto;
import com.backendSiege.domain.service.AlerteService;
import com.backendSiege.infrastructure.mapper.AlerteMapper;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class AlerteController {

    private final AlerteService alerteService;
    private final AlerteMapper alerteMapper;

    @GetMapping("/alertes")
    public List<AlerteDto> getAlertes(String pays, UUID idEntrepot,
                                      @RequestParam(value = "startDate", required = false) LocalDateTime startDate,
                                      @RequestParam(value = "endDate", required = false) LocalDateTime endDate) {

        return alerteService.getAllAlertesByEntrepotId(pays, idEntrepot, startDate, endDate ).stream()
                .map(alerteMapper::toAlerteDto)
                .toList();
    }
}
