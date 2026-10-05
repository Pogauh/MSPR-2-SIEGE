package com.backendSiege.api.controller;

import com.backendSiege.api.dto.HistoriqueResponseDto;
import com.backendSiege.api.mapper.HistoriqueMapper;
import com.backendSiege.domain.models.TypeHistoriqueEnum;
import com.backendSiege.domain.services.HistoriqueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class HistoriqueController {

    private final HistoriqueService historiqueService;
    private final HistoriqueMapper historiqueMapper;

    public HistoriqueController(HistoriqueService historiqueService, HistoriqueMapper historiqueMapper) {
        this.historiqueService = historiqueService;
        this.historiqueMapper = historiqueMapper;
    }

    /** L'historique d'un lot ; {@code type} (TRANSFERT, ALERTE ou ALERT, SHIPPED, sans tenir compte de la casse) est facultatif. */
    @GetMapping("/history/{idLot}")
    public ResponseEntity<List<HistoriqueResponseDto>> getAllHistoriqueForLot(@PathVariable UUID idLot,
                                                                             @RequestParam(value = "type", required = false) String type) {
        TypeHistoriqueEnum filtre = null;
        if (type != null && !type.isBlank()) {
            try {
                filtre = TypeHistoriqueEnum.depuisSaisie(type);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok(historiqueService.findAllHistoriqueForLot(idLot, filtre).stream()
                .map(historiqueMapper::toResponse)
                .toList());
    }
}
