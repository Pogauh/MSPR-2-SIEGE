package com.backendSiege.api.controller;

import com.backendSiege.domain.model.Entrepot;
import com.backendSiege.domain.model.Exploitation;
import com.backendSiege.domain.service.ReferentielService;
import com.backendSiege.infrastructure.configuration.WebClientConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReferentielController {

    private final WebClientConfig webClientConfig;
    private final ReferentielService referentielService;


    public ReferentielController(WebClientConfig webClientConfig, ReferentielService referentielService) {
        this.webClientConfig = webClientConfig;
        this.referentielService = referentielService;
    }

    @GetMapping("/pays")
    public ResponseEntity<Map<String, WebClientConfig.CountrySettings>> getAllPays() {
        return ResponseEntity.ok(webClientConfig.getMapping());
    }

    @GetMapping("/exploitation/{pays}")
    public ResponseEntity<List<Exploitation>> getAllPays(@PathVariable String pays) {
        List<Exploitation> result = referentielService.getAllExploitationsByPays(pays);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/allEntrepotByPays/{pays}")
    public ResponseEntity<List<Entrepot>> getAllEntrepotByPays(@PathVariable String pays) {
        List<Entrepot> result = referentielService.getAllEntrepotsByPays(pays);
        return ResponseEntity.ok(result);
    }


}
