package com.backendSiege.api.controller;

import com.backendSiege.api.dto.ZoneResponseDto;
import com.backendSiege.domain.model.Entrepot;
import com.backendSiege.domain.model.Exploitation;
import com.backendSiege.domain.service.ReferentielService;
import com.backendSiege.infrastructure.configuration.WebClientConfig;
import com.backendSiege.infrastructure.mapper.ZoneMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class ReferentielController {

    private final WebClientConfig webClientConfig;
    private final ReferentielService referentielService;
    private final ZoneMapper zoneMapper;


    public ReferentielController(WebClientConfig webClientConfig, ReferentielService referentielService, ZoneMapper zoneMapper) {
        this.webClientConfig = webClientConfig;
        this.referentielService = referentielService;
            this.zoneMapper = zoneMapper;
    }

    @GetMapping("/countries")
    public ResponseEntity<Map<String, WebClientConfig.CountrySettings>> getAllPays() {
        return ResponseEntity.ok(webClientConfig.getMapping());
    }

    @GetMapping("/farms/{pays}")
    public ResponseEntity<List<Exploitation>> getAllPays(@PathVariable String pays) {
        List<Exploitation> result = referentielService.getAllExploitationsByPays(pays);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/warehouses/{pays}")
    public ResponseEntity<List<Entrepot>> getAllEntrepotByPays(@PathVariable String pays) {
        List<Entrepot> result = referentielService.getAllEntrepotsByPays(pays);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/warehouses/{entrepotId}/zones")
    public ResponseEntity<List<ZoneResponseDto>> getAllZonesByEntrepot(@PathVariable UUID entrepotId, @RequestParam String pays) {
        List<ZoneResponseDto> result = referentielService.findAllZonesByEntrepot(pays, entrepotId)
                .stream()
                .map(zoneMapper::toDtoResponse)
                .toList();
        return ResponseEntity.ok(result);
    }

}