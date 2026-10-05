package com.backendSiege.api.controller;

import com.backendSiege.api.dto.EntrepotDto;
import com.backendSiege.api.dto.ExploitationDto;
import com.backendSiege.api.dto.PaysDto;
import com.backendSiege.api.dto.ZoneResponseDto;
import com.backendSiege.api.mapper.ReferentielMapper;
import com.backendSiege.domain.exceptions.PaysIndisponibleException;
import com.backendSiege.domain.models.Pays;
import com.backendSiege.domain.services.ReferentielService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Référentiel : pays, exploitations, entrepôts et zones. */
@RestController
public class ReferentielController {

    private final ReferentielService referentielService;
    private final ReferentielMapper referentielMapper;

    public ReferentielController(ReferentielService referentielService, ReferentielMapper referentielMapper) {
        this.referentielService = referentielService;
        this.referentielMapper = referentielMapper;
    }

    /** Les pays configurés, par nom, avec l'adresse de leur backend et leur plage climatique idéale. */
    @GetMapping("/countries")
    public ResponseEntity<Map<String, PaysDto>> getAllPays() {
        Map<String, PaysDto> pays = new LinkedHashMap<>();
        for (Pays p : referentielService.getAllPays()) {
            pays.put(p.code(), referentielMapper.toDto(p));
        }
        return ResponseEntity.ok(pays);
    }

    /** Les exploitations d'un pays ; un pays qui ne répond pas donne un {@code 200} sans corps (comportement historique). */
    @GetMapping("/farms/{pays}")
    public ResponseEntity<List<ExploitationDto>> getAllExploitationsByPays(@PathVariable String pays) {
        try {
            return ResponseEntity.ok(referentielService.getAllExploitationsByPays(pays).stream().map(referentielMapper::toDto).toList());
        } catch (PaysIndisponibleException e) {
            return ResponseEntity.ok().build();
        }
    }

    /** Les entrepôts d'un pays ; un pays qui ne répond pas donne un {@code 200} sans corps (comportement historique). */
    @GetMapping("/warehouses/{pays}")
    public ResponseEntity<List<EntrepotDto>> getAllEntrepotsByPays(@PathVariable String pays) {
        try {
            return ResponseEntity.ok(referentielService.getAllEntrepotsByPays(pays).stream().map(referentielMapper::toDto).toList());
        } catch (PaysIndisponibleException e) {
            return ResponseEntity.ok().build();
        }
    }

    /** Les zones d'un entrepôt, avec leur nombre de lots. */
    @GetMapping("/warehouses/{entrepotId}/zones")
    public ResponseEntity<List<ZoneResponseDto>> getAllZonesByEntrepot(@PathVariable UUID entrepotId, @RequestParam String pays) {
        return ResponseEntity.ok(referentielService.getAllZonesByEntrepot(pays, entrepotId).stream()
                .map(referentielMapper::toResponse)
                .toList());
    }
}
